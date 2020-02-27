package com.github.halotroop.litecraft.logic;

import tk.valoeghese.sod.BinaryData;

public interface DataStorage {
	void read(BinaryData data);
	void write(BinaryData data);
}
