package org.apache.lucene.queryParser.aqp.processors;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Iterator;

import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.processors.NoChildOptimizationQueryNodeProcessor;
import org.apache.lucene.queryParser.core.processors.QueryNodeProcessor;
import org.apache.lucene.queryParser.core.processors.QueryNodeProcessorPipeline;
import org.apache.lucene.queryParser.core.processors.RemoveDeletedQueryNodesProcessor;
import org.apache.lucene.queryParser.standard.builders.StandardQueryTreeBuilder;
import org.apache.lucene.queryParser.standard.config.StandardQueryConfigHandler;
import org.apache.lucene.queryParser.standard.parser.StandardSyntaxParser;
import org.apache.lucene.queryParser.standard.processors.AllowLeadingWildcardProcessor;
import org.apache.lucene.queryParser.standard.processors.AnalyzerQueryNodeProcessor;
import org.apache.lucene.queryParser.standard.processors.BooleanSingleChildOptimizationQueryNodeProcessor;
import org.apache.lucene.queryParser.standard.processors.BoostQueryNodeProcessor;
import org.apache.lucene.queryParser.standard.processors.DefaultPhraseSlopQueryNodeProcessor;
import org.apache.lucene.queryParser.standard.processors.FuzzyQueryNodeProcessor;
import org.apache.lucene.queryParser.standard.processors.GroupQueryNodeProcessor;
import org.apache.lucene.queryParser.standard.processors.LowercaseExpandedTermsQueryNodeProcessor;
import org.apache.lucene.queryParser.standard.processors.MatchAllDocsQueryNodeProcessor;
import org.apache.lucene.queryParser.standard.processors.MultiFieldQueryNodeProcessor;
import org.apache.lucene.queryParser.standard.processors.MultiTermRewriteMethodProcessor;
import org.apache.lucene.queryParser.standard.processors.ParametricRangeQueryNodeProcessor;
import org.apache.lucene.queryParser.standard.processors.PhraseSlopQueryNodeProcessor;
import org.apache.lucene.queryParser.standard.processors.RemoveEmptyNonLeafQueryNodeProcessor;
import org.apache.lucene.queryParser.standard.processors.WildcardQueryNodeProcessor;
import org.apache.lucene.search.Query;

/**
 * This is based on the standard/processors
 *
 * This pipeline has all the processors needed to process a query node tree,
 * generated by {@link StandardSyntaxParser}, already assembled. <br/>
 * <br/>
 * The order they are assembled affects the results. <br/>
 * <br/>
 * This processor pipeline was designed to work with
 * {@link StandardQueryConfigHandler}. <br/>
 * <br/>
 * The result query node tree can be used to build a {@link Query} object using
 * {@link StandardQueryTreeBuilder}. <br/>
 *
 * @see StandardQueryTreeBuilder
 * @see StandardQueryConfigHandler
 * @see StandardSyntaxParser
 */
public class AqpQueryNodeProcessorPipeline extends
    QueryNodeProcessorPipeline {

  public AqpQueryNodeProcessorPipeline(QueryConfigHandler queryConfig) {
    super(queryConfig);
    
    add(new AqpDEFOPProcessor());
    add(new AqpTreeRewriteProcessor());
    add(new AqpQPHRASEProcessor());
    add(new AqpQPHRASETRUNCProcessor());
    //add(new AqpBOOSTProcessor());
    
    add(new AqpOPERATORProcessor());
    add(new AqpVALUEProcessor());
    add(new AqpNUCLEUSProcessor());
    //add(new AqpMULTIATOMProcessor());
    add(new AqpMULTITERMProcessor());
    add(new AqpATOMProcessor());
    add(new AqpCLAUSEProcessor());
    
    
    //add(new AqpMODIFIERProcessor());
    
    add(new WildcardQueryNodeProcessor());
    add(new MultiFieldQueryNodeProcessor());
    add(new FuzzyQueryNodeProcessor());
    add(new MatchAllDocsQueryNodeProcessor());
    add(new LowercaseExpandedTermsQueryNodeProcessor());
    add(new ParametricRangeQueryNodeProcessor());
    add(new AllowLeadingWildcardProcessor());
    add(new AnalyzerQueryNodeProcessor());
    add(new PhraseSlopQueryNodeProcessor());
    //add(new GroupQueryNodeProcessor());
    add(new NoChildOptimizationQueryNodeProcessor());
    add(new RemoveDeletedQueryNodesProcessor());
    add(new RemoveEmptyNonLeafQueryNodeProcessor());
    add(new BooleanSingleChildOptimizationQueryNodeProcessor());
    add(new DefaultPhraseSlopQueryNodeProcessor());
    add(new BoostQueryNodeProcessor());
    add(new MultiTermRewriteMethodProcessor());
    add(new AqpOptimizationProcessor());
  }
  
  
}
