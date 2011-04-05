package com.pas.cosc322.amazon.search;

import static com.pas.cosc322.amazon.main.Main.debug;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * A state space search implementation.  Used for single player games or
 * multiplayer games that have reached a state where the opponent's actions
 * does not effect your future actions.
 * 
 * @author Paul
 *
 * @param <S> the state type
 * @param <A> the action type
 * @param <M> the player type
 */
public class StateSpaceSearch <S extends State<A, M>, A extends Action, M extends MinimaxPlayer>
{
	private int maxThreads;
	private long startTime;
	
	private M player;
	
	private volatile int cutoffDepth = 1;
	
	private AtomicInteger localMaxDepth = new AtomicInteger();
	private AtomicBoolean isCutoff = new AtomicBoolean();

	/**
	 * Constructor
	 * 
	 * @param maxThreads the max threads to use in the thread pool
	 */
	public StateSpaceSearch (int maxThreads)
	{
		this.maxThreads = maxThreads;
	}
	
	/**
	 * @return the current depth at which a search will end during IDS
	 */
	public int getCutoffDepth ()
	{
		return cutoffDepth;
	}
	
	/**
	 * @param cutoffDepth the depth to end a search during IDS
	 */
	public void setCutoffDepth (int cutoffDepth)
	{
		this.cutoffDepth = cutoffDepth;
	}

	/**
	 * Searches the game tree and returns the best possible action according
	 * to the evaluation function and bounded by the cutoff function.
	 * 
	 * @param player the player
	 * @param state initial state
	 * @return an action
	 */
	@SuppressWarnings("unchecked")
	public A statespaceDecision (M player, S state)
	{
		// record the start time of the search for the cutoff functions
		startTime = System.currentTimeMillis();
		this.player = player;
		// cutoff depth for IDS
		// the best result of any search
		SearchResult<A> globalBest = new SearchResult<A>(Integer.MIN_VALUE, null);
		// construct the search sub trees, they will persist through each iteration
		List<StateSpaceSearchThread> searchThreads = new LinkedList<StateSpaceSearchThread>();
		for (Queue<A> actions = state.actions(player); !actions.isEmpty();)
		{
			searchThreads.add(new StateSpaceSearchThread((S) state.clone(), actions.remove()));
		}
		// begin IDS
		do
		{
			// the best depth achieved by this search
			localMaxDepth.set(1);
			// if this search was terminated by a cutoff test
			isCutoff.set(false);
			// construct our thread pool
			ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
			List<Future<SearchResult<A>>> results = new LinkedList<Future<SearchResult<A>>>();
			// supply each worker thread with a subtree
			for (StateSpaceSearchThread searchThread : searchThreads)
			{
				results.add(executor.submit(searchThread));
			}
			SearchResult<A> localBest = new SearchResult(Integer.MIN_VALUE, null);
			try
			{
				// close the pool and wait for execution completion
				executor.shutdown();
				// we'll assume the timeout occurs naturally from the search cutoff test
				executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
				// find the best result for this search
				SearchResult search;
				for (Future<SearchResult<A>> result : results)
				{
					search = result.get();
					localBest.max(search);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			// IMPORTANT: only update the global max if we have truly achieved a
			// deeper search this iteration by not being cutoff.  This could produce
			// a false positive if the search was cutoff with mixed depths.
			if (!isCutoff.get() || globalBest.action == null)
			{
				globalBest.max(localBest);
			}
			cutoffDepth++;
		}
		// if we didn't make the target depth then we won't make a deeper target depth next iteration; end the search
		while (!isCutoff.get() && localMaxDepth.get() >= cutoffDepth - 1);
		cutoffDepth -= 2;
		debug.logp(Level.INFO, "StateSpaceSearch", "statespaceDecision", "Search took:"+(System.currentTimeMillis()-startTime)+" maximum depth:"+cutoffDepth+" best value:"+globalBest.v);
		return globalBest.action;
	}

	/**
	 * A Callable search to be executed on a thread.
	 * 
	 * @author Paul
	 */
	private class StateSpaceSearchThread implements Callable<SearchResult<A>>
	{
		private S state;
		private A parentAction;
		
		/**
		 * Constructor.
		 * 
		 * @param state the initial state
		 * @param action the action for the initial state, used for callback
		 */
		public StateSpaceSearchThread (S state, A action)
		{
			this.state = state;
			parentAction = action;
			state.applyAction(action);
		}

		@Override
		public SearchResult<A> call () throws Exception
		{
			// note depth 2, results from depth 1 are being collected in statespaceDecision
			int depth = 2;
			localMaxDepth.set(Math.max(depth, localMaxDepth.get()));
			// test the cutoff function
			if (state.cutoffTest(depth, startTime))
			{
				isCutoff.set(true);
				return new SearchResult<A>(state.evaluate(player), parentAction);
			}
			if (depth >= cutoffDepth)
			{
				return new SearchResult<A>(state.evaluate(player), parentAction);
			}
			Queue<A> successors = state.actions(player);
			// we can end the search here if there are no successors
			if (successors.isEmpty())
			{
				return new SearchResult<A>(state.evaluate(player), parentAction);
			}
			A action;
			SearchResult<A> result = new SearchResult<A>(Integer.MIN_VALUE, parentAction);
			int v = Integer.MIN_VALUE;
			while (!successors.isEmpty())
			{
				action = successors.remove();
				state.applyAction(action);
				// always try to maximise the score
				v = Math.max(v, maxValue(depth + 1));
				// because we are using one state instance make sure to undo the action during back-tracking!
				state.undoAction(action);
			}
			result.v = v;
			return result;
		}

		private int maxValue (int depth)
		{
			localMaxDepth.set(Math.max(depth, localMaxDepth.get()));
			// test for IDS cutoff and the cutoff function
			if (state.cutoffTest(depth, startTime))
			{
				isCutoff.set(true);
				return state.evaluate(player);				
			}
			if (depth >= cutoffDepth)
			{
				return state.evaluate(player);
			}
			// actions for player
			Queue<A> successors = state.actions(player);
			// we can end the search here if there are no successors
			if (successors.isEmpty())
			{
				return state.evaluate(player);
			}
			A action;
			int v = Integer.MIN_VALUE;
			// standard state space search
			while (!successors.isEmpty())
			{
				action = successors.remove();
				state.applyAction(action);
				// always try to maximise the score
				v = Math.max(v, maxValue(depth + 1));
				// because we are using one state instance make sure to undo the action during back-tracking!
				state.undoAction(action);
			}
			return v;
		}
	}
}
