package com.ecommerce.microcommerce.web.controller;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.web.exceptions.ProduitGratuitException;
import com.ecommerce.microcommerce.web.exceptions.ProduitIntrouvableException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Api(description = "API pour les opérations CRUD sur les produits.")

@RestController
public class ProductController {

    @Autowired
    private ProductDao productDao;

    @RequestMapping(value="/produits", method = RequestMethod.GET)
    public MappingJacksonValue listeProduits() {
        List<Product> products = productDao.findAll();

        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");
        FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);
        MappingJacksonValue productsFilters = new MappingJacksonValue(products);
        productsFilters.setFilters(listDeNosFiltres);
        return productsFilters;
    }

    @GetMapping(value="/produits/{id}")
    public Product afficherUnProduit(@PathVariable int id) {

        Product produit = productDao.findById(id);
        if(produit==null) throw new ProduitIntrouvableException("Le produit avec l'id " + id + " est INTROUVABLE. Écran Bleu si je pouvais.");
        return produit;
    }

    @PostMapping(value="/produits")
    public ResponseEntity<Void> ajouterProduit(@RequestBody Product product) {

        if(product.getPrix()==0) throw new ProduitGratuitException("Le prix de vente ne peut pas être égal à 0");

        Product productAdded = productDao.save(product);
        if(productAdded==null)
            return ResponseEntity.noContent().build();

        URI locattion = ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(productAdded.getId())
                        .toUri();
        return ResponseEntity.created(locattion).build();
    }

    @PutMapping(value = "/produits")
    public void updateProduit(@RequestBody Product product) {
        productDao.save(product);
    }

    @DeleteMapping(value = "/produits/{id}")
    public void supprimerProduit(@PathVariable int id) {
        productDao.delete(id);
    }

    @GetMapping(value = "test/products/{price}")
    public List<Product> testeDeRequetes(@PathVariable int price) {
        return productDao.chercherUnProduitCher(400);
    }

    @GetMapping(value="/adminProduits")
    public List<String> calculerMargeProduits() {
        List<Product> products = productDao.findAll();
        List<String> listMargin = new ArrayList<>();
        products.forEach(
                (product) -> {
                    int margin = product.getPrix() - product.getPrixAchat();
                    listMargin.add(product.toString()+":"+margin);
                }
        );
        return listMargin;
    }

    @GetMapping(value="/produitsByNom")
    public List<Product> trierProduitsParOrdreAlphabetique() {
        return productDao.findAllOrderByNom();
    }


}
