package com.spark.collectibles.routes;

import com.spark.collectibles.model.Product;
import com.spark.collectibles.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static spark.Spark.*;

/**
 * View routes for rendering HTML templates
 * 
 * This class defines all the view routes that serve HTML pages
 * using Mustache templates for the web interface.
 */
public class ViewRoutes {
    private static final Logger logger = LoggerFactory.getLogger(ViewRoutes.class);
    private static final MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();
    
    /**
     * Initialize all view routes
     * @param productService ProductService instance for business logic
     */
    public static void initialize(ProductService productService) {
        
        // Home page
        get("/", (request, response) -> {
            logger.info("GET / - Rendering home page");
            Map<String, Object> model = new HashMap<>();
            model.put("title", "Home");
            model.put("content", "<h2>Welcome to Collectibles Store</h2><p>Browse our amazing collection of collectible items!</p><a href='/products' class='btn btn-primary'>Browse Products</a>");
            return new ModelAndView(model, "base.mustache");
        }, templateEngine);
        
        // Login page
        get("/login", (request, response) -> {
            logger.info("GET /login - Rendering login page");
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "login.mustache");
        }, templateEngine);
        
        // Register page
        get("/register", (request, response) -> {
            logger.info("GET /register - Rendering register page");
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "register.mustache");
        }, templateEngine);
        
        // Profile page
        get("/profile", (request, response) -> {
            logger.info("GET /profile - Rendering profile page");
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "profile.mustache");
        }, templateEngine);
        
        // Products list page
        get("/products", (request, response) -> {
            logger.info("GET /products - Rendering products list page");
            Map<String, Object> model = new HashMap<>();
            
            try {
                String query = request.queryParams("q");
                String category = request.queryParams("category");
                String minPriceStr = request.queryParams("min");
                String maxPriceStr = request.queryParams("max");
                
                List<Product> products;
                
                // Apply filters
                if (query != null && !query.trim().isEmpty()) {
                    products = productService.searchProducts(query.trim());
                } else if (category != null && !category.trim().isEmpty()) {
                    products = productService.getProductsByCategory(category.trim());
                } else if (minPriceStr != null || maxPriceStr != null) {
                    // Support filtering with only min, only max, or both
                    try {
                        BigDecimal minPrice = null;
                        BigDecimal maxPrice = null;
                        
                        if (minPriceStr != null && !minPriceStr.trim().isEmpty()) {
                            minPrice = new BigDecimal(minPriceStr.trim());
                        }
                        
                        if (maxPriceStr != null && !maxPriceStr.trim().isEmpty()) {
                            maxPrice = new BigDecimal(maxPriceStr.trim());
                        }
                        
                        products = productService.getProductsByPriceRange(minPrice, maxPrice);
                    } catch (NumberFormatException e) {
                        products = productService.getAllProducts();
                    }
                } else {
                    products = productService.getAllProducts();
                }
                
                model.put("products", products);
                model.put("searchQuery", query != null ? query : "");
                model.put("minPrice", minPriceStr != null ? minPriceStr : "");
                model.put("maxPrice", maxPriceStr != null ? maxPriceStr : "");
                
                // Get unique categories for filter dropdown
                Set<String> categories = products.stream()
                    .map(Product::getCategory)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
                model.put("categories", new ArrayList<>(categories));
                
                // Mark selected category
                if (category != null) {
                    Map<String, Boolean> selected = new HashMap<>();
                    selected.put("selected", true);
                    // Note: In a real implementation, you'd set this on the specific category option
                }
                
            } catch (Exception e) {
                logger.error("Error loading products for view", e);
                model.put("products", new ArrayList<>());
                model.put("errorMessage", "Error loading products: " + e.getMessage());
            }
            
            return new ModelAndView(model, "products.mustache");
        }, templateEngine);
        
        // Admin product management - list all products
        get("/admin/products", (request, response) -> {
            logger.info("GET /admin/products - Rendering admin product form");
            Map<String, Object> model = new HashMap<>();
            
            try {
                List<Product> allProducts = productService.getAllProducts();
                model.put("allProducts", allProducts);
                model.put("product", null); // No product selected for create mode
            } catch (Exception e) {
                logger.error("Error loading products for admin view", e);
                model.put("allProducts", new ArrayList<>());
                model.put("errorMessage", "Error loading products: " + e.getMessage());
            }
            
            return new ModelAndView(model, "admin/product-form.mustache");
        }, templateEngine);
        
        // Admin product management - edit specific product
        get("/admin/products/:id", (request, response) -> {
            String id = request.params(":id");
            logger.info("GET /admin/products/{} - Rendering edit product form", id);
            
            Map<String, Object> model = new HashMap<>();
            
            try {
                Product product = productService.getProductById(id);
                if (product == null) {
                    response.status(404);
                    return createErrorModel("Product not found", 404, "Product Not Found", 
                                          "The product with ID '" + id + "' was not found.");
                }
                
                // Prepare product model with currency flags for template
                Map<String, Object> productModel = new HashMap<>();
                productModel.put("id", product.getId());
                productModel.put("name", product.getName());
                productModel.put("description", product.getDescription());
                productModel.put("price", product.getPrice());
                productModel.put("currency", product.getCurrency());
                productModel.put("category", product.getCategory());
                
                // Set currency flags for template selection
                productModel.put("currencyUSD", "USD".equals(product.getCurrency()));
                productModel.put("currencyEUR", "EUR".equals(product.getCurrency()));
                productModel.put("currencyGBP", "GBP".equals(product.getCurrency()));
                
                model.put("product", productModel);
                
                // Load all products for the table
                List<Product> allProducts = productService.getAllProducts();
                model.put("allProducts", allProducts);
                
            } catch (Exception e) {
                logger.error("Error loading product for edit", e);
                response.status(500);
                return createErrorModel("Error loading product", 500, "Internal Server Error", 
                                      "An error occurred while loading the product: " + e.getMessage());
            }
            
            return new ModelAndView(model, "admin/product-form.mustache");
        }, templateEngine);
        
        // Handle form submission for creating/updating products
        post("/admin/products", (request, response) -> {
            logger.info("POST /admin/products - Processing product creation");
            return handleProductFormSubmission(request, response, productService, false);
        });
        
        post("/admin/products/:id", (request, response) -> {
            String id = request.params(":id");
            logger.info("POST /admin/products/{} - Processing product update", id);
            return handleProductFormSubmission(request, response, productService, true);
        });
        
        // Error pages
        get("/error/:code", (request, response) -> {
            String codeStr = request.params(":code");
            int statusCode = 500;
            String errorTitle = "Internal Server Error";
            String errorMessage = "An unexpected error occurred.";
            
            try {
                statusCode = Integer.parseInt(codeStr);
                switch (statusCode) {
                    case 404:
                        errorTitle = "Not Found";
                        errorMessage = "The page you're looking for doesn't exist.";
                        break;
                    case 400:
                        errorTitle = "Bad Request";
                        errorMessage = "Invalid request. Please check your input.";
                        break;
                    case 403:
                        errorTitle = "Forbidden";
                        errorMessage = "You don't have permission to access this resource.";
                        break;
                    case 500:
                        errorTitle = "Internal Server Error";
                        errorMessage = "An error occurred on the server.";
                        break;
                }
            } catch (NumberFormatException e) {
                // Use defaults
            }
            
            response.status(statusCode);
            Map<String, Object> model = new HashMap<>();
            model.put("statusCode", statusCode);
            model.put("errorTitle", errorTitle);
            model.put("errorMessage", errorMessage);
            
            return new ModelAndView(model, "error.mustache");
        }, templateEngine);
    }
    
    /**
     * Handle product form submission (create or update)
     */
    private static Object handleProductFormSubmission(spark.Request request, 
                                                      spark.Response response, 
                                                      ProductService productService,
                                                      boolean isUpdate) {
        Map<String, Object> model = new HashMap<>();
        
        try {
            // Parse form data (from HTML form submission)
            String id = request.queryParams("id");
            String name = request.queryParams("name");
            String description = request.queryParams("description");
            String priceStr = request.queryParams("price");
            String currency = request.queryParams("currency");
            String category = request.queryParams("category");
            
            if (id == null || id.trim().isEmpty() ||
                name == null || name.trim().isEmpty() ||
                description == null || description.trim().isEmpty() ||
                priceStr == null || priceStr.trim().isEmpty() ||
                currency == null || currency.trim().isEmpty()) {
                
                response.status(400);
                model.put("errorMessage", "Missing required fields");
                model.put("allProducts", productService.getAllProducts());
                model.put("product", null);
                return new ModelAndView(model, "admin/product-form.mustache");
            }
            
            BigDecimal price;
            try {
                price = new BigDecimal(priceStr);
            } catch (NumberFormatException e) {
                response.status(400);
                model.put("errorMessage", "Invalid price format");
                model.put("allProducts", productService.getAllProducts());
                model.put("product", null);
                return new ModelAndView(model, "admin/product-form.mustache");
            }
            
            Product product = new Product();
            product.setId(id.trim());
            product.setName(name.trim());
            product.setDescription(description.trim());
            product.setPrice(price);
            product.setCurrency(currency.trim());
            product.setCategory(category != null && !category.trim().isEmpty() ? category.trim() : null);
            
            Product result;
            if (isUpdate) {
                result = productService.updateProduct(id, product);
            } else {
                result = productService.createProduct(product);
            }
            
            if (result == null) {
                response.status(400);
                model.put("errorMessage", isUpdate ? "Failed to update product" : "Failed to create product");
                model.put("allProducts", productService.getAllProducts());
                model.put("product", null);
            } else {
                response.redirect("/admin/products");
                return null; // Redirect handled
            }
            
        } catch (Exception e) {
            logger.error("Error processing product form submission", e);
            response.status(500);
            model.put("errorMessage", "An error occurred: " + e.getMessage());
            model.put("allProducts", productService.getAllProducts());
            model.put("product", null);
        }
        
        return new ModelAndView(model, "admin/product-form.mustache");
    }
    
    /**
     * Create error model for error pages
     */
    private static ModelAndView createErrorModel(String logMessage, int statusCode, 
                                                 String errorTitle, String errorMessage) {
        logger.error(logMessage);
        Map<String, Object> model = new HashMap<>();
        model.put("statusCode", statusCode);
        model.put("errorTitle", errorTitle);
        model.put("errorMessage", errorMessage);
        return new ModelAndView(model, "error.mustache");
    }
}

