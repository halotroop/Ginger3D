package tk.valoeghese.sod;

import java.util.*;

/**
 * Represents a section of SOD data.
 * @author Valoeghese
 */
public class DataSection implements Iterable<Object> {
	public DataSection() {
		this.data = new ArrayList<>();
	}

	private final List<Object> data; 

	public void writeByte(byte data) {
		this.data.add(data);
	}

	public void writeShort(short data) {
		this.data.add(data);
	}

	public void writeInt(int data) {
		this.data.add(data);
	}

	public void writeLong(long data) {
		this.data.add(data);
	}

	public void writeFloat(float data) {
		this.data.add(data);
	}

	public void writeDouble(double data) {
		this.data.add(data);
	}

	public void writeString(String data) {
		this.data.add(data);
	}

	public void writeBoolean(boolean data) {
		this.data.add(data ? (byte) 1 : (byte) 0);
	}
	
	public <T extends Enum<?>> void writeEnum(T enumValue) {
		this.data.add(enumValue.ordinal());
	}

	public <T extends Enum<?>> void writeEnumAsString(T enumValue) {
		this.data.add(enumValue.toString());
	}

	public int size() {
		return this.data.size();
	}

	public byte readByte(int index) {
		return (byte) this.data.get(index);
	}

	public short readShort(int index) {
		return (short) this.data.get(index);
	}

	public int readInt(int index) {
		return (int) this.data.get(index);
	}

	public long readLong(int index) {
		return (long) this.data.get(index);
	}

	public float readFloat(int index) {
		return (float) this.data.get(index);
	}

	public double readDouble(int index) {
		return (double) this.data.get(index);
	}

	public String readString(int index) {
		return (String) this.data.get(index);
	}

	public boolean readBoolean(int index) {
		return ((byte) this.data.get(index)) != 0;
	}

	public <T extends Enum<T>> T readEnumString(int index, Class<T> type) {
		return Enum.valueOf(type, (String) this.data.get(index));
	}

	public <T extends Enum<?>> T readEnum(int index, T[] values) {
		return values[(int) this.data.get(index)];
	}

	@Override
	public Iterator<Object> iterator() {
		return this.data.iterator();
	}
}
