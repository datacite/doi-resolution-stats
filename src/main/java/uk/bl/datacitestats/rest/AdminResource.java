package uk.bl.datacitestats.rest;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.bl.datacitestats.services.loader.LogLoadReport;
import uk.bl.datacitestats.services.loader.LogMarshaller;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class AdminResource extends SelfInjectingServerResource{

	Logger log = LoggerFactory.getLogger(AdminResource.class);
	LogLoadReport latestReport;
	
	@Inject
	LogMarshaller m;
	static AtomicBoolean lock = new AtomicBoolean(false);
	ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
    Callable<LogLoadReport> reload = new Callable<LogLoadReport>() {
        @Override
        public LogLoadReport call() throws Exception {
            return m.findAndParse();
        }
    };
    
	public enum ACTION_TYPE {
		RELOAD;
		public static Optional<ACTION_TYPE> fromString(String s) {
			try {
				return Optional.of(ACTION_TYPE.valueOf(s.toUpperCase()));
			} catch (IllegalArgumentException | NullPointerException e) {
				return Optional.absent();
			}
		}
	}
	
	@Get 
	public Representation getReport(){
		Optional<ACTION_TYPE> action = ACTION_TYPE.fromString(this.getAttribute("action"));
		if (!action.isPresent()){
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
		switch(action.get()){
			case RELOAD:{
				if (latestReport != null){
					JacksonRepresentation<LogLoadReport> rep = new JacksonRepresentation<LogLoadReport>(latestReport);
					return rep;
				}
			}
		}
		setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
		return null;
	}
	
	@Post
	public void runJob(){
		Optional<ACTION_TYPE> action = ACTION_TYPE.fromString(this.getAttribute("action"));
		if (!action.isPresent()){
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
		switch(action.get()){
			case RELOAD:{
				reload();
			}
		}
	}

	private void reload(){
		if (lock.compareAndSet(false, true)){
			/*
		    CachingProvider provider = Caching.getCachingProvider();
		    CacheManager manager = provider.getCacheManager();
		    for (String c :manager.getCacheNames()){
		    	manager.getCache(c).removeAll();
		    }*/
		    ListenableFuture<LogLoadReport> listenableFuture = executor.submit(reload);				 
		    Futures.addCallback(listenableFuture, new FutureCallback<LogLoadReport>() {
		        public void onSuccess(LogLoadReport result) {
		        	latestReport = result;
		        	log.info("reloaded logs "+result);
		        	lock.set(false);
		        }
		        public void onFailure(Throwable thrown) {
		        	latestReport = null;
		        	log.info("reloaded log failiure",thrown);
		        	lock.set(false);
		        }
		    });
			this.setStatus(Status.SUCCESS_ACCEPTED);
		}else{
			this.setStatus(Status.INFO_PROCESSING);
		}
	}
}
