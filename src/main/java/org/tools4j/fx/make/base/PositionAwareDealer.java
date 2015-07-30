package org.tools4j.fx.make.base;

import java.util.Objects;

import org.tools4j.fx.make.api.ExecutionResult;
import org.tools4j.fx.make.api.Order;
import org.tools4j.fx.make.api.Settings;
import org.tools4j.fx.make.api.Dealer;

/**
 * A {@link Dealer} accepting all orders as long as the position is within the
 * allowed max size.
 * <p>
 * The class is thread safe and lock free.
 */
public class PositionAwareDealer implements Dealer {
	
	private final Settings settings;
	private final PositionKeeper position = new PositionKeeper();

	public PositionAwareDealer(Settings settings) {
		this.settings = Objects.requireNonNull(settings, "settings is null");
	}

	@Override
	public ExecutionResult acceptOrReject(Order order) {
		if (position.updateIfNotExceedsMax(order, settings.getMaxAllowedPositionSize())) {
			return ExecutionResult.FILLED;
		}
		return ExecutionResult.REJECTED;
	}
	
	public long getPosition() {
		return position.getPosition();
	}

	public void resetPosition() {
		position.resetPosition();
	}
}
