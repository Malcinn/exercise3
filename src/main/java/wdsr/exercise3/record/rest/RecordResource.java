package wdsr.exercise3.record.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wdsr.exercise3.record.Record;
import wdsr.exercise3.record.RecordInventory;

@Path("/records")
public class RecordResource {
	private static final Logger log = LoggerFactory.getLogger(RecordResource.class);
	
	@Inject
	private RecordInventory recordInventory;
	
	/**
	 * TODO Add methods to this class that will implement the REST API described below.
	 * Use methods on recordInventory to create/read/update/delete records. 
	 * RecordInventory instance (a CDI bean) will be injected at runtime.
	 * Content-Type used throughout this exercise is application/xml.
	 * API invocations that must be supported:
	 */
	
	/**
	 * * GET https://localhost:8091/records
	 * ** Returns a list of all records (as application/xml)
	 * ** Response status: HTTP 200
	 */
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getRecords(){
		List<Record> results = this.recordInventory.getRecords();
		GenericEntity<List<Record>> genericResults = new GenericEntity<List<Record>>(results) {};
		return Response.ok(genericResults).build();
	}

	/**
	 * POST https://localhost:8091/records
	 * ** Creates a new record, returns ID of the new record.
	 * ** Consumes: record (as application/xml), ID must be null.
	 * ** Response status if ok: HTTP 201, Location header points to the newly created resource.
	 * ** Response status if submitted record has ID set: HTTP 400
	 */
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	public Response createRecord(Record record, @Context UriInfo uriInfo){
		if (record.getId() == null){
			int recordId = 0;
			UriBuilder uriBuilder;
			try{
				 recordId = this.recordInventory.addRecord(record);
			} catch(IllegalArgumentException e){
				return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
			}
			uriBuilder = uriInfo.getAbsolutePathBuilder();
			uriBuilder.path(Integer.toString(recordId));
			return Response.created(uriBuilder.build()).build();
		} else {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}
	
	
	
	/**
	 * * GET https://localhost:8091/records/{id}
	 * ** Returns an existing record (as application/xml)
	 * ** Response status if ok: HTTP 200
	 * ** Response status if {id} is not known: HTTP 404
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getRecord(@PathParam(value = "id") int id){
		Record record = this.recordInventory.getRecord(id);
		if (record != null){
			return Response.status(Status.OK).entity(record).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}
	 /**
	 * * PUT https://localhost:8091/records/{id}
	 * ** Replaces an existing record in entirety.
	 * ** Submitted record (as application/xml) must have null ID or ID must be identical to {id} in the path.
	 * ** Response status if ok: HTTP 204
	 * ** Response status if {id} is not known: HTTP 404
	 */
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_XML)
	public Response putRecord(Record record, @PathParam(value = "id") int id){
		if (record.getId() == null || record.getId().equals(id))
			record.setId(id);
		else
			return Response.status(Status.BAD_REQUEST).build();
		
		if (this.recordInventory.updateRecord(id, record)){
			return Response.status(Status.NO_CONTENT).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
		
	}
	
	/**
	 * * DELETE https://localhost:8091/records/{id}
	 * ** Deletes an existing record.
	 * ** Response status if ok: HTTP 204
	 * ** Response status if {id} is not known: HTTP 404
	 */
	@DELETE
	@Path("/{id}")
	public Response deleteRecord(@PathParam("id") int id){
		if (this.recordInventory.deleteRecord(id)){
			return Response.status(Status.NO_CONTENT).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}
}
