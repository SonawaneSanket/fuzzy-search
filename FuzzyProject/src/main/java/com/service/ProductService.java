package com.service;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.product.Product;
import com.repo.productRepo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    @Autowired
    private productRepo productRepository;

    public List<Product> searchProductsFuzzy(String searchTerm) throws IOException, ParseException {
        List<Product> allProducts = productRepository.findAll();
        Analyzer analyzer = new StandardAnalyzer();
        
        Directory memoryIndex = new RAMDirectory();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(memoryIndex, indexWriterConfig);

        for (Product product : allProducts) {
            Document doc = new Document();
            doc.add(new StringField("id", product.getId().toString(), Field.Store.YES));
            doc.add(new TextField("name", product.getName(), Field.Store.YES));
            indexWriter.addDocument(doc);
        }
        
        indexWriter.close();
        
        IndexReader indexReader = DirectoryReader.open(memoryIndex);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        QueryParser queryParser = new QueryParser("name", analyzer);
        Query query = queryParser.parse(searchTerm);

        TopDocs topDocs = indexSearcher.search(query, allProducts.size());
        List<Product> matchingProducts = new ArrayList<>();
        
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            int docId = scoreDoc.doc;
            Document doc = indexSearcher.doc(docId);
            Long productId = Long.valueOf(doc.get("id"));
            matchingProducts.add(productRepository.findById(productId).orElse(null));
        }

        return matchingProducts;
    }
}
