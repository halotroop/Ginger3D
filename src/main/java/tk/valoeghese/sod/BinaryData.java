package tk.valoeghese.sod;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import tk.valoeghese.sod.exception.SODParseException;

public class BinaryData implements Iterable<Map.Entry<String, DataSection>> {
	public BinaryData() {
		this.sections = new HashMap<>();
	}

	private final Map<String, DataSection> sections;

	public DataSection get(String name) {
		return this.sections.get(name);
	}

	public DataSection getOrCreate(String name) {
		return this.sections.computeIfAbsent(name, k -> new DataSection());
	}

	public void put(String name, DataSection section) {
		this.sections.put(name, section);
	}

	public boolean write(File file) {
		try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
			Parser.write(this, dos);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public Iterator<Map.Entry<String, DataSection>> iterator() {
		return this.sections.entrySet().iterator();
	}

	public static BinaryData read(File file) throws SODParseException {
		try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
			long magic = dis.readLong();

			if (magic != 0xA77D1E) {
				throw new SODParseException("Not a valid SOD file!");
			}

			return Parser.parse(dis);
		} catch (IOException e) {
			e.printStackTrace();
			//throw new SODParseException("Error in parsing file " + file.toString());
			return new BinaryData();
		}
	}
}
