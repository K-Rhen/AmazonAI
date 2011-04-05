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
 * A Minimax search algorithm with makes use of Alpha-Beta Pruning,
 * Iterative Deepening Search, and Best-First techniques.  Also uses
 * a thread pool to concurrently search branches.
 * 
 * @author Paul
 * 
 * @param <S> State type
 * @param <A> Action type
 * @param <M> MinimaxPlayer type
 */
public class MinimaxSearch <S extends State<A, M>, A extends Action, M extends MinimaxPlayer>
{
	private int maxThreads;
	private long startTime;
	
	private M maxPlayer, minPlayer;
	
	private volatile int cutoffDepth = 1;
	
	private AtomicInteger localMaxDepth = new AtomicInteger();
	private AtomicBoolean isCutoff = new AtomicBoolean();

	/**
	 * Constructor
	 * 
	 * @param maxThreads the max threads to use in the thread pool
	 */
	public MinimaxSearch (int maxThreads)
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
	 * @param maxPlayer the MAX player
	 * @param minPlayer the MIN player
	 * @param state initial state
	 * @return an action
	 */
	@SuppressWarnings("unchecked")
	public A minimaxDecision (M maxPlayer, M minPlayer, S state)
	{
		// record the start time of the search for the cutoff functions
		startTime = System.currentTimeMillis();
		this.maxPlayer = maxPlayer;
		this.minPlayer = minPlayer;
		// cutoff depth for IDS
		// the best result of any search
		SearchResult<A> globalBest = new SearchResult<A>(Integer.MIN_VALUE, null);
		// construct the search sub trees, they will persist through each iteration
		List<MinimaxSearchThread> searchThreads = new LinkedList<MinimaxSearchThread>();
		for (Queue<A> actions = state.actions(maxPlayer); !actions.isEmpty();)
		{
			searchThreads.add(new MinimaxSearchThread((S) state.clone(), actions.remove()));
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
			for (MinimaxSearchThread searchThread : searchThreads)
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
		debug.logp(Level.INFO, "MinimaxSearch", "minimaxDecision", "Search took:"+(System.currentTimeMillis()-startTime)+" maximum depth:"+cutoffDepth+" best value:"+globalBest.v);
		return globalBest.action;
	}

	/**
	 * A Callable alpha-beta search to be executed on a thread.
	 * 
	 * @author Paul
	 */
	private class MinimaxSearchThread implements Callable<SearchResult<A>>
	{
		private S state;
		private A parentAction;
		
		/**
		 * Constructor.
		 * 
		 * @param state the initial state
		 * @param action the action for the initial state, used for callback
		 */
		public MinimaxSearchThread (S state, A action)
		{
			this.state = state;
			parentAction = action;
			state.applyAction(action);
		}

		@Override
		public SearchResult<A> call () throws Exception
		{
			// note depth 2, results from depth 1 are being collected in minimaxDecision
			int depth = 2;
			localMaxDepth.set(Math.max(depth, localMaxDepth.get()));
			// test the cutoff function
			if (state.cutoffTest(depth, startTime))
			{
				isCutoff.set(true);
				return new SearchResult<A>(state.evaluate(maxPlayer), parentAction);
			}
			if (depth >= cutoffDepth)
			{
				return new SearchResult<A>(state.evaluate(maxPlayer), parentAction);
			}
			Queue<A> successors = state.actions(minPlayer);
			// we can end the search here if there are no successors
			if (successors.isEmpty())
			{
				return new SearchResult<A>(state.evaluate(maxPlayer), parentAction);
			}
			A action;
			SearchResult<A> result = new SearchResult<A>(Integer.MIN_VALUE, parentAction);
			int v = Integer.MAX_VALUE;
			int alpha = Integer.MIN_VALUE, beta = Integer.MAX_VALUE;
			while (!successors.isEmpty())
			{
				action = successors.remove();
				state.applyAction(action);
				v = Math.min(v, maxValue(alpha, beta, depth + 1));
				// because we are using one state instance make sure to undo the action during back-tracking!
				state.undoAction(action);
				if (v <= alpha)
				{
					break;
				}
				beta = Math.min(beta, v);
			}
			result.v = v;
			return result;
		}
		

		private int maxValue (int alpha, int beta, int depth)
		{
			localMaxDepth.set(Math.max(depth, localMaxDepth.get()));
			// test for IDS cutoff and the cutoff function
			if (state.cutoffTest(depth, startTime))
			{
				isCutoff.set(true);
				return state.evaluate(maxPlayer);				
			}
			if (depth >= cutoffDepth)
			{
				return state.evaluate(maxPlayer);
			}
			// actions for MAX player
			Queue<A> successors = state.actions(maxPlayer);
			// we can end the search here if there are no successors
			if (successors.isEmpty())
			{
				return state.evaluate(maxPlayer);
			}
			A action;
			int v = Integer.MIN_VALUE;
			// standard alpha-beta search
			while (!successors.isEmpty())
			{
				action = successors.remove();
				state.applyAction(action);
				v = Math.max(v, minValue(alpha, beta, depth + 1));
				// because we are using one state instance make sure to undo the action during back-tracking!
				state.undoAction(action);
				if (v >= beta)
				{
					break;
				}
				alpha = Math.max(alpha, v);
			}
			return v;
		}

		private int minValue (int alpha, int beta, int depth)
		{
			localMaxDepth.set(Math.max(depth, localMaxDepth.get()));
			// test for IDS cutoff and the cutoff function
			if (state.cutoffTest(depth, startTime))
			{
				isCutoff.set(true);
				return state.evaluate(maxPlayer);
			}
			if (depth >= cutoffDepth)
			{
				return state.evaluate(maxPlayer);
			}
			// actions for MIN player
			Queue<A> successors = state.actions(minPlayer);
			// we can end the search here if there are no successors
			if (successors.isEmpty())
			{
				return state.evaluate(maxPlayer);
			}
			A action;
			int v = Integer.MAX_VALUE;
			// standard alpha-beta search
			while (!successors.isEmpty())
			{
				action = successors.remove();
				state.applyAction(action);
				v = Math.min(v, maxValue(alpha, beta, depth + 1));
				// because we are using one state instance make sure to undo the action during back-tracking!
				state.undoAction(action);
				if (v <= alpha)
				{
					break;
				}
				beta = Math.min(beta, v);
			}
			return v;
		}
	}
}
