

import os
import thread
import sys
import cStringIO

from invenio import search_engine
from invenio import search_engine_summarizer
from invenio import dbquery
from invenio import bibrecord
from invenio.intbitset import intbitset
from invenio import bibrank_citation_searcher as bcs



from StringIO import StringIO as sIO

def dispatch(func_name, *args, **kwargs):
    """Dispatches the call to the *local* worker
    It returns a tuple (ThreadID, result)
    """
    tid = thread.get_ident()
    out = globals()[func_name](*args, **kwargs)
    return [tid, out]


def get_recids_changes(last_recid, max_recs=10000, mod_date=None):
    """
    Retrieves the sets of records that were added/updated/deleted
    
    The time is selected according to some know recid, ie. 
    we retrieve the modification time of one record and look
    at those that are older.
    
        OR
        
    You can pass in the date that you are interested in, in the 
    format: YYYY-MM-DD HH:MM:SS
    
    added => bibrec.modification_date == bibrec.creation_date
    updated => bibrec.modification_date >= bibrec.creation_date
    deleted => bibrec.status == DELETED
    """
    search_op = '>'    
    if not mod_date:
        if last_recid == -1:
            l = list(dbquery.run_sql("SELECT modification_date FROM bibrec ORDER BY modification_date ASC LIMIT 1"))
            mod_date = l[0][0].strftime(format="%Y-%m-%d %H:%M:%S")
            search_op = '>='
        else:
            # let's make sure we have a valid recid (or get the close valid one)
            l = list(dbquery.run_sql("SELECT id, modification_date FROM bibrec WHERE id >= %s LIMIT 1", (last_recid,)))
            if not len(l):
                return
            last_recid = l[0][0]
            mod_date = l[0][1].strftime(format="%Y-%m-%d %H:%M:%S")
            
            # there is not api to get this (at least i haven't found it)
            #mod_date = search_engine.get_modification_date(last_recid, fmt="%Y-%m-%d %H:%i:%S")
            #if not mod_date:
            #    return
        
    modified_records = list(dbquery.run_sql("SELECT id,modification_date, creation_date FROM bibrec "
                    "WHERE modification_date " + search_op + "\"%s\" ORDER BY modification_date ASC, id ASC LIMIT %s" %
                    (mod_date, max_recs )))
    
    #sys.stderr.write('%s, %s, %s\n' % (last_recid, mod_date,
    #    "SELECT id,modification_date, creation_date FROM bibrec "
    #    "WHERE modification_date " + search_op + "\"%s\" ORDER BY modification_date ASC, id ASC LIMIT %s" %
    #    (mod_date, max_recs )))
    
    #print len(modified_records)
    
    if not len(modified_records):
        return
    
    added = []
    updated = []
    deleted = []
    
    for recid, mod_date, create_date in modified_records:
        
        #rec = search_engine.get_record(recid)
        #status = bibrecord.record_get_field_value(rec, tag='980', code='c')
        status = search_engine.record_exists(recid)
        
        if status == -1:
            deleted.append(int(recid))
        elif mod_date == create_date:
            added.append(int(recid))
        else:
            updated.append(int(recid))
    
    return {'DELETED': deleted, 'UPDATED': updated, 'ADDED': added}, int(recid), str(mod_date)


def citation_summary(recids, of, ln, p, f):
    out = ReqStringIO()
    x = search_engine_summarizer.summarize_records(recids, of, ln, p, f, out)
    if x:
        output = x
    else:
        out.seek(0)
        output = out.read()
    return output

def invenio_search(kwargs):
    """This search uses Invenio API to retrieve anything"""
    
    # because of invenio bug, sanity checking of rg is wrong
    if 'rg' in kwargs:
        kwargs['rg'] = int(kwargs['rg'])
    
    req = cStringIO.StringIO()
    result = search_engine.perform_request_search(req=req, **kwargs)
    data = None
    if not result:
        req.seek(0)
        data = req.read()
    req.close()
    if result:
        return result
    if data:
        return data
    

def invenio_search_xml(kwargs):
    """Simple version which just fetches XML records
    from Invenio. It only understands query of type:
    p=recid:1->50 OR recid:50 OR recid:....
    
    Unfortunately, we cannot use 'print_records' because
    that one (for strange reasons) creates a range out 
    of recIDS. And I don't want to use bibformat, 
    because bibformat is not working nicely with 
    strings (it is slower)
    
    """
    out = []
    p = kwargs['p']
    of = 'xm'
    if 'of' in kwargs:
        of = kwargs['of']

    if of == 'xm':
        out.append('<?xml version="1.0" encoding="UTF-8"?>')
        out.append('<collection xmlns="http://www.loc.gov/MARC21/slim">')
    
    clauses = p.split(' OR ')
    for c in clauses:
        c = c.replace('recid:', '')
        if '->' in c:
            ints = c.split('->')
            for x in xrange(int(ints[0]), int(ints[1])+1):
                out.append(search_engine.print_record(x, format=of))
        else:
            out.append(search_engine.print_record(int(c), format=of))
    if of == 'xm':
        out.append('</collection>')
    
    return '\n'.join(out)
    


def search(q, max_len=25, offset=0):
    #hits = search_engine.search_pattern_parenthesised(None, q)
    hits = search_engine.perform_request_search(None, p=q)
    total_matches = len(hits)

    if max_len:
        return [offset, hits[:max_len], total_matches]
    else:
        return [offset, hits, total_matches]


def sort_and_format(hits, kwargs):

    kwargs = search_engine.prs_wash_arguments(**kwargs)
    t1 = os.times()[4]
    req = ReqStringIO()  # new ver of Invenio is looking at args
    kwargs['req'] = req

    if 'hosted_colls_actual_or_potential_results_p' not in kwargs:
        kwargs['hosted_colls_actual_or_potential_results_p'] = True # this prevents display of the nearest-term box

    # search stage 4 and 5: intersection with collection universe and sorting/limiting
    output = search_engine.prs_intersect_with_colls_and_apply_search_limits(hits, kwargs=kwargs, **kwargs)
    if output is not None:
        req.seek(0)
        return req.read() + output

    t2 = os.times()[4]
    cpu_time = t2 - t1
    kwargs['cpu_time'] = cpu_time

    recids = search_engine.prs_rank_results(kwargs=kwargs, **kwargs)

    if 'of' in kwargs and kwargs['of'].startswith('hc'):
        output = citation_summary(intbitset(recids), kwargs['of'], kwargs['ln'], kwargs['p'], kwargs['f'])
        if output:
            return output

    return recids


def get_citation_dict(dictname):
    return bcs.get_citation_dict(dictname)



class ReqStringIO(sIO):
    '''Because of Invenio insisting to have args inside the 
    req object, we cannot use cStringIO'''
    
    def __init__(self, *args, **kwargs):
        sIO.__init__(self, *args, **kwargs)
        self.args = None
        self.uri = None

if __name__ == '__main__':
    pass
