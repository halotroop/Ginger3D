package tk.valoeghese.sod;

import java.io.*;
import java.util.*;

import tk.valoeghese.sod.exception.SODParseException;

enum DataType
{
	SECTION(0),
	BYTE(1),
	SHORT(2),
	INT(3),
	LONG(4),
	FLOAT(5),
	DOUBLE(6),
	STRING(7);

	public static DataType of(byte id) throws SODParseException
	{
		switch (id)
		{
		case 0:
			return SECTION;
		case 1:
			return BYTE;
		case 2:
			return SHORT;
		case 3:
			return INT;
		case 4:
			return LONG;
		case 5:
			return FLOAT;
		case 6:
			return DOUBLE;
		case 7:
			return STRING;
		default:
			throw new SODParseException("Unknown data type " + String.valueOf(id));
		}
	}

	public final byte id;

	private DataType(int id)
	{ this.id = (byte) id; }
}

final class Parser
{
	static BinaryData parse(DataInputStream input) throws IOException, SODParseException
	{
		BinaryData data = new BinaryData();
		DataType dataType;
		dataType = DataType.of(input.readByte());
		if (dataType != DataType.SECTION)
		{ throw new SODParseException("Data must be segregated into sections!"); }
		DataSection currentSection = new DataSection();
		data.put(input.readUTF(), currentSection);
		while (input.available() > 0)
		{
			dataType = DataType.of(input.readByte());
			switch (dataType)
			{
			case BYTE:
				currentSection.writeByte(input.readByte());
				break;
			case DOUBLE:
				currentSection.writeDouble(input.readDouble());
				break;
			case FLOAT:
				currentSection.writeFloat(input.readFloat());
				break;
			case INT:
				currentSection.writeInt(input.readInt());
				break;
			case LONG:
				currentSection.writeLong(input.readLong());
				break;
			case SECTION:
				currentSection = new DataSection();
				data.put(input.readUTF(), currentSection);
				break;
			case SHORT:
				currentSection.writeShort(input.readShort());
				break;
			case STRING:
				currentSection.writeString(input.readUTF());
				break;
			default:
				throw new RuntimeException("This error should never be thrown! If this error occurs, the parser is not properly dealing with a most-likely-invalid data type.");
			}
		}
		return data;
	}

	static void write(BinaryData data, DataOutputStream dos) throws IOException
	{
		dos.writeLong(0xA77D1E);
		Iterator<Map.Entry<String, DataSection>> sectionStream = data.iterator();
		while (sectionStream.hasNext())
		{
			Map.Entry<String, DataSection> section = sectionStream.next();
			dos.writeByte(DataType.SECTION.id);
			dos.writeUTF(section.getKey());
			Iterator<Object> dataStream = section.getValue().iterator();
			while (dataStream.hasNext())
			{
				Object o = dataStream.next();
				if (o instanceof Byte)
				{
					dos.writeByte(DataType.BYTE.id);
					dos.writeByte((byte) o);
				}
				else if (o instanceof Short)
				{
					dos.writeByte(DataType.SHORT.id);
					dos.writeShort((short) o);
				}
				else if (o instanceof Integer)
				{
					dos.writeByte(DataType.INT.id);
					dos.writeInt((int) o);
				}
				else if (o instanceof Long)
				{
					dos.writeByte(DataType.LONG.id);
					dos.writeLong((long) o);
				}
				else if (o instanceof Float)
				{
					dos.writeByte(DataType.FLOAT.id);
					dos.writeFloat((float) o);
				}
				else if (o instanceof Double)
				{
					dos.writeByte(DataType.DOUBLE.id);
					dos.writeDouble((double) o);
				}
				else if (o instanceof String)
				{
					dos.writeByte(DataType.STRING.id);
					dos.writeUTF((String) o);
				}
			}
		}
	}
}