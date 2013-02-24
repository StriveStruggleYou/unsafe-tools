package com.alexkasko.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of {@link OffHeapMemory} using {@code sun.misc.Unsafe}
 *
 * @author alexkasko
 * Date: 1/14/13
 */
class UnsafeOffHeapMemory extends OffHeapMemory {

    private static final Unsafe UNSAFE;
    private static final int BYTE_ARRAY_OFFSET;

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
            int boo = UNSAFE.arrayBaseOffset(byte[].class);
            // It seems not all Unsafe implementations implement the following method.
            UNSAFE.copyMemory(new byte[1], boo, new byte[1], boo, 1);
            BYTE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final long address;
    private final long length;
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    UnsafeOffHeapMemory(long bytes) {
        this.address = UNSAFE.allocateMemory(bytes);
        this.length = bytes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUnsafe() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long length() {
        return length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void free() {
        if(!disposed.compareAndSet(false, true)) return;
        UNSAFE.freeMemory(address);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void finalize() throws Throwable {
        free();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(long offset, byte[] buffer, int bufferOffset, int bytes) {
        assert offset >= 0 : offset;
        assert offset < length : offset;
        assert bufferOffset >= 0 : bufferOffset;
        assert bytes > 0 : bytes;
        assert bytes <= length : bytes;
        assert bufferOffset <= buffer.length - bytes : bufferOffset;
        UNSAFE.copyMemory(buffer, BYTE_ARRAY_OFFSET + bufferOffset, null, address + offset, bytes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(long offset, byte[] buffer) {
        assert offset >= 0 : offset;
        assert offset < length : offset;
        assert null != buffer;
        UNSAFE.copyMemory(buffer, 0, null, address + offset, buffer.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void get(long offset, byte[] buffer, int bufferOffset, int bytes) {
        assert offset >= 0 : offset;
        assert offset < length : offset;
        assert bufferOffset >= 0 : bufferOffset;
        assert bytes > 0 : bytes;
        assert bytes <= length : bytes;
        assert bufferOffset <= buffer.length - bytes : bufferOffset;
        UNSAFE.copyMemory(null, address + offset, buffer, BYTE_ARRAY_OFFSET + bufferOffset, bytes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void get(long offset, byte[] buffer) {
        assert offset >= 0 : offset;
        assert offset < length : offset;
        assert null != buffer;
        UNSAFE.copyMemory(null, address + offset, buffer, 0, buffer.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte getByte(long offset) {
        assert offset >= 0 : offset;
        assert offset <= length - 1 : offset;
        return UNSAFE.getByte(address + offset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putByte(long offset, byte value) {
        assert offset >= 0 : offset;
        assert offset <= length - 1 : offset;
        UNSAFE.putByte(address + offset, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getUnsignedByte(long offset) {
        assert offset >= 0 : offset;
        assert offset <= length - 1 : offset;
        return (short) (UNSAFE.getByte(address + offset) & 0xff);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putUnsignedByte(long offset, short value) {
        assert offset >= 0 : offset;
        assert offset <= length - 1 : offset;
        assert value >= 0 : value;
        assert value < 1<<8 : value;
        UNSAFE.putByte(address + offset, (byte) value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getShort(long offset) {
        assert offset >= 0 : offset;
        assert offset <= length - 2 : offset;
        return UNSAFE.getShort(address + offset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putShort(long offset, short value) {
        assert offset >= 0 : offset;
        assert offset <= length - 2 : offset;
        UNSAFE.putShort(address + offset, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getUnsignedShort(long offset) {
        assert offset >= 0 : offset;
        assert offset <= length - 2 : offset;
        return UNSAFE.getShort(address + offset) & 0xffff;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putUnsignedShort(long offset, int value) {
        assert offset >= 0 : offset;
        assert offset <= length - 2 : offset;
        assert value >= 0 : value;
        assert value < 1<<16 : value;
        UNSAFE.putShort(address + offset, (short) value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getInt(long offset) {
        assert offset >= 0 : offset;
        assert offset <= length - 4 : offset;
        return UNSAFE.getInt(address + offset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putInt(long offset, int value) {
        assert offset >= 0 : offset;
        assert offset <= length - 4 : offset;
        UNSAFE.putInt(address + offset, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getUnsignedInt(long offset) {
        assert offset >= 0 : offset;
        assert offset <= length - 4 : offset;
        return UNSAFE.getInt(address + offset) & 0xffffffffL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putUnsignedInt(long offset, long value) {
        assert offset >= 0 : offset;
        assert offset <= length - 4 : offset;
        assert value >= 0 : value;
        assert value < 1L<<32 : value;
        UNSAFE.putInt(address + offset, (int) value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLong(long offset) {
        assert offset >= 0 : offset;
        assert offset <= length - 8 : offset;
        return UNSAFE.getLong(address + offset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putLong(long offset, long value) {
        assert offset >= 0 : offset;
        assert offset <= length - 8 : offset;
        UNSAFE.putLong(address + offset, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("UnsafeOffHeapMemory");
        sb.append("{address=").append(address);
        sb.append(", length=").append(length);
        sb.append(", disposed=").append(disposed);
        sb.append('}');
        return sb.toString();
    }
}