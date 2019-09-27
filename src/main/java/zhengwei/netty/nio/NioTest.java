package zhengwei.netty.nio;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;

/**
 * @author zhengwei AKA Awei
 * @since 2019/9/22 16:45
 */
public class NioTest {
	private ByteBuffer buffer;

	@Test
	void testNioBuffer() {
		//缓冲区
		IntBuffer buffer = IntBuffer.allocate(10);
		for (int i = 0; i < buffer.capacity(); i++) {
			int random = new SecureRandom().nextInt(20);
			buffer.put(random);
		}
		//读写切换，把读转成写，把写转成读
		buffer.flip();
		while (buffer.hasRemaining()) {
			System.out.println(buffer.get());
		}
	}

	@Test
	void testInputStreamToNio() {
		try (FileInputStream fileInputStream = new FileInputStream("e:/restartcac.sh");
		     FileChannel fileChannel = fileInputStream.getChannel()) {
			ByteBuffer buffer = ByteBuffer.allocate(512);
			fileChannel.read(buffer);
			buffer.flip();
			while (buffer.remaining() > 0) {
				final byte b = buffer.get();
				System.out.println("char->" + (char) b);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void testOutputStreamToNio() {
		try (FileOutputStream fileOutputStream = new FileOutputStream("e:/niotest.txt");
		     FileChannel channel = fileOutputStream.getChannel()) {
			ByteBuffer buffer = ByteBuffer.allocate(512);
			final byte[] message = "hello world".getBytes();
			for (byte b : message) {
				//向缓冲区中写数据即buffer读取数据
				buffer.put(b);
			}
			//由读数据转换成写数据
			buffer.flip();
			channel.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Test
	void test1() {
		//1.分配一个指定大小的缓冲区
		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
		System.out.println("-------------allocate-----------------");
		System.out.println(byteBuffer.position());
		System.out.println(byteBuffer.limit());
		System.out.println(byteBuffer.capacity());
		//2.利用put()存入数据
		String str="abcd";
		byteBuffer.put(str.getBytes());
		System.out.println("-------------put-----------------");
		System.out.println(byteBuffer.position());
		System.out.println(byteBuffer.limit());
		System.out.println(byteBuffer.capacity());
		//3.切换成读取数据的模式  filp()
		byteBuffer.flip();//切换到读取数据的模式
		System.out.println("-------------filp-----------------");
		System.out.println(byteBuffer.position());
		System.out.println(byteBuffer.limit());
		System.out.println(byteBuffer.capacity());
		//4.读取缓存区的数据
		byte[] dst=new byte[byteBuffer.limit()];
		byteBuffer.get(dst);
		System.out.println("-------------filp-----------------");
		System.out.println(dst);
		System.out.println(byteBuffer.position());
		System.out.println(byteBuffer.limit());
		System.out.println(byteBuffer.capacity());
		//5.rewind()可重读
		byteBuffer.rewind();
		System.out.println("-------------rewind-----------------");
		System.out.println(byteBuffer.position());
		System.out.println(byteBuffer.limit());
		System.out.println(byteBuffer.capacity());
		//6.清空缓冲区，但缓冲区的数据还在，但处于被遗忘状态
		byteBuffer.clear();
		System.out.println("-------------clear-----------------");
		System.out.println(byteBuffer.position());
		System.out.println(byteBuffer.limit());
		System.out.println(byteBuffer.capacity());
		//7.mark()
		byteBuffer.flip();
		byteBuffer.get(dst, 0, 2);
		System.out.println(byteBuffer.position());
		byteBuffer.mark();
		byteBuffer.get(dst, 2, 2);
		System.out.println(byteBuffer.position());
		byteBuffer.reset();
		System.out.println(byteBuffer.position());
		//8.remaining()
		byteBuffer.remaining();//剩余可操作数据的长度
		byteBuffer.hasRemaining();//是否还有剩余可操作数据
	}

}