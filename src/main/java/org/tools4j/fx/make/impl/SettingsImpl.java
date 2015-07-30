package org.tools4j.fx.make.impl;

import org.tools4j.fx.make.api.Settings;

public class SettingsImpl implements Settings {
	private final long maxAllowedPositionSize;

	public SettingsImpl(long maxAllowedPositionSize) {
		if (maxAllowedPositionSize < 0) {
			throw new IllegalArgumentException("max allowed position size cannot be negative: " + maxAllowedPositionSize);
		}
		this.maxAllowedPositionSize = maxAllowedPositionSize;
	}

	public long getMaxAllowedPositionSize() {
		return maxAllowedPositionSize;
	}
}
