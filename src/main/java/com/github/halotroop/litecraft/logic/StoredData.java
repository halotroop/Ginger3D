package com.github.halotroop.litecraft.logic;

import tk.valoeghese.sod.BinaryData;

public interface StoredData {
	void read(BinaryData data);
	void write(BinaryData data);
}
