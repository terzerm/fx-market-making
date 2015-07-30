package org.tools4j.fx.make.api;

/**
 * A market participant that is willing to take orders.
 */
public interface Dealer {
	
	ExecutionResult acceptOrReject(Order order);
	
	/**
	 * A dealer accepting all orders.
	 */
	Dealer ACCEPT_ALL = new Dealer() {
		@Override
		public ExecutionResult acceptOrReject(Order order) {
			return ExecutionResult.FILLED;
		}
	};
	/**
	 * A dealer rejecting all orders.
	 */
	Dealer REJECT_ALL = new Dealer() {
		@Override
		public ExecutionResult acceptOrReject(Order order) {
			return ExecutionResult.REJECTED;
		}
	};
}
