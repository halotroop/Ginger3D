package tk.valoeghese.sod;

import java.io.*;

public class TestMain {

	public static void main(String[] args) throws IOException {
		//writeTest()
		readTest();
	}

	static void readTest() throws IOException {
		File f = new File("../test.sod");

		if (!f.createNewFile()) {
			BinaryData bd = BinaryData.read(f);

			DataSection ds1 = bd.get("DS1");

			for (Object i : ds1) {
				System.out.println(i);
			}

			DataSection ds2 = bd.get("yeet");

			for (Object i : ds2) {
				System.out.println(i);
			}
		}
	}

	static void writeTest() throws IOException {
		File f = new File("../test.sod");
		f.createNewFile();
		BinaryData bd = new BinaryData();

		DataSection ds1 = bd.getOrCreate("DS1");
		ds1.writeBoolean(false);
		ds1.writeDouble(0.666D);
		ds1.writeLong(69696969);
		ds1.writeString("yaY33T");

		DataSection ds2 = bd.getOrCreate("yeet");
		ds2.writeByte((byte) 4);
		ds2.writeFloat(1.3F);
		ds2.writeString("e");
		ds2.writeString("ff");

		bd.write(f);
	}

}
