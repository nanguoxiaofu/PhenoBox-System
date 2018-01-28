package at.gmi.djamei.phenopipe;

import java.util.HashMap;


import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.reactivex.Observable;

//TODO Move to shared library
/**
 * Singleton to hold references to Observables which hold Progress responses generated by background tasks
 * @author Sebastian Seitner
 * 
 */
public enum JobMapper {
	INSTANCE;
	static final Logger logger = LogManager.getLogger();
	private final HashMap<UUID, Observable<ProgressResponse>> job2Observable=new HashMap<>();
	
	/**
	 * Add a Job which status observable will not complete on a specific message. Such jobs should be removed from the mapper manually
	 * by calling JobMapper.INSTANCE.remove(UUID jobId)
	 * @param jobID the id of the job to add
	 * @param obs the Observable of the ProgressResponses
	 */
	public void put(UUID jobID, Observable<ProgressResponse> obs){
		put(jobID,obs,null);
	}
	/**
	 * Adds a Jobs status Observable to the mapper which will be evicted when the status Observable emits a message that is equal to completeOn.
	 * Additionally the Observable which is stored will complete on the appearance of the String.
	 * @param jobID the id of the job to add
	 * @param obs the Observable of the ProgressResponse
	 * @param completeOn The message of a progressResponse on which the observable should complete. 
	 */
	public void put(UUID jobID, Observable<ProgressResponse> obs, String completeOn){
		obs= obs.takeWhile(resp->!resp.getMessage().equals(completeOn))
				.doOnTerminate(()->{this.remove(jobID);});
		job2Observable.put(jobID, obs);
		logger.debug("Added job {}", jobID);
	}
	/**
	 * Removes the entry identified by the given ID from the Mapper
	 * @param jobID The id of the job which should be removed
	 */
	public void remove(UUID jobID){
		job2Observable.remove(jobID);
		logger.debug("Removed job {}", jobID);
	}
	/**
	 * Fetches the Observable which belongs to the given job ID
	 * @param jobID the id of the job for which the observable should be fetched
	 * @return The Observable of ProgressResponse which belongs to the job
	 */
	public Observable<ProgressResponse> get(UUID jobID){
		return job2Observable.get(jobID);
	}
}
