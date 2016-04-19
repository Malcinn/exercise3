package wdsr.exercise3.client;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import wdsr.exercise3.model.Product;
import wdsr.exercise3.model.ProductType;

public class ProductService extends RestClientBase {

	protected ProductService(final String serverHost, final int serverPort, final Client client) {
		super(serverHost, serverPort, client);
	}

	/**
	 * Looks up all products of given types known to the server.
	 * 
	 * @param types
	 *            Set of types to be looked up
	 * @return A list of found products - possibly empty, never null.
	 */
	public List<Product> retrieveProducts(Set<ProductType> types) {
		WebTarget productsTarget = this.baseTarget.path("/products").queryParam("type", types.toArray());
		Response response = productsTarget.request(MediaType.APPLICATION_JSON).get(Response.class);
		List<Product> listOfProducts = response.readEntity(new GenericType<List<Product>>() {});
		return listOfProducts;

	}

	/**
	 * Looks up all products known to the server.
	 * 
	 * @return A list of all products - possibly empty, never null.
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public List<Product> retrieveAllProducts() {
		WebTarget allProductsTarget = this.baseTarget.path("/products");
		Response response = allProductsTarget.request(MediaType.APPLICATION_JSON).get(Response.class);
		List<Product> listOfProducts = response.readEntity(new GenericType<List<Product>>(){});
		return listOfProducts;
	}

	/**
	 * Looks up the product for given ID on the server.
	 * 
	 * @param id
	 *            Product ID assigned by the server
	 * @return Product if found
	 * @throws NotFoundException
	 *             if no product found for the given ID.
	 */
	public Product retrieveProduct(int id) throws NotFoundException {
		WebTarget productTarget = this.baseTarget.path("/products/" + id);
		Response response = productTarget.request(MediaType.APPLICATION_JSON).get(Response.class);
		
		if (response.getStatus() == 404)
			throw new NotFoundException(response.readEntity(String.class));
		
		Product product = response.readEntity(Product.class);

		return product;
	}

	/**
	 * Creates a new product on the server.
	 * 
	 * @param product
	 *            Product to be created. Must have null ID field.
	 * @return ID of the new product.
	 * @throws WebApplicationException
	 *             if request to the server failed
	 */
	public int storeNewProduct(Product product) throws WebApplicationException {
		int idOfNewProduct = 0;

		WebTarget newProduct = this.baseTarget.path("/products");
		Response response = newProduct.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(product, MediaType.APPLICATION_JSON));
		 
		if (response.getStatus() >= 400) {
			throw new WebApplicationException();
		}

		URI responseURI = response.getLocation();
		int lastSlash = responseURI.toString().lastIndexOf("/");
		idOfNewProduct = Integer.parseInt(responseURI.toString().substring(lastSlash + 1));
		response.close();
		
		return idOfNewProduct;
	}

	/**
	 * Updates the given product.
	 * 
	 * @param product
	 *            Product with updated values. Its ID must identify an existing
	 *            resource.
	 * @throws NotFoundException
	 *             if no product found for the given ID.
	 */
	public void updateProduct(Product product) {
		WebTarget updatedProduct = this.baseTarget.path("/products/" + product.getId());
		Response response = updatedProduct.request(MediaType.APPLICATION_JSON)
				.put(Entity.entity(product, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 404) {
			throw new NotFoundException(response.readEntity(String.class));
		}
	}

	/**
	 * Deletes the given product.
	 * 
	 * @param product
	 *            Product to be deleted. Its ID must identify an existing
	 *            resource.
	 * @throws NotFoundException
	 *             if no product found for the given ID.
	 */
	public void deleteProduct(Product product) {
		WebTarget deletedProduct = this.baseTarget.path("/products/" + product.getId());
		Response response = deletedProduct.request(MediaType.APPLICATION_JSON).delete();
		if (response.getStatus() == 404) {
			throw new NotFoundException(response.readEntity(String.class));
		}
	}
}
