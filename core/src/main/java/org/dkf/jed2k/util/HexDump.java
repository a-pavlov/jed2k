package org.dkf.jed2k.util;

/**
 * Created by inkpot on 18.01.2017.
 */
public class HexDump {
    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private static boolean isAsciiPrintable(char ch) {
        return ch >= 32 && ch < 127;
    }

    private static void appendCharacters(final byte[] data, final StringBuilder sb, int startPos, int processedBytes) {
        if (processedBytes == 0) return;
        assert startPos >= 0;
        assert startPos + processedBytes <= data.length;
        int endBorder = startPos + processedBytes;

        if (endBorder % 16 != 0) {
            for(int i = 0; i < (16 - processedBytes); ++i) {
                sb.append("   ");
            }
        }

        if (startPos < endBorder) sb.append(" | ");

        for(int i = startPos; i < endBorder; ++i) {
            char c = (char)data[i];
            sb.append(isAsciiPrintable(c)?c:'.');
        }
    }

    public static String dump(final byte[] bytes, int offset, int count) {
        assert bytes != null;
        assert bytes.length >= offset + count;
        assert offset >= 0 && count >= 0;
        StringBuilder sb = new StringBuilder();

        int processedBytes = 0;
        int j;
        for (j = 0; j < count; j++ ) {
            if (j != 0 && j % 16 == 0) {
                appendCharacters(bytes, sb, j + offset - processedBytes, processedBytes);
                processedBytes = 0;
                sb.append("\n");
            }

            int v = bytes[j + offset] & 0xFF;
            sb.append(hexArray[v >>> 4]).append(hexArray[v & 0x0F]).append(" ");
            ++processedBytes;
        }

        appendCharacters(bytes, sb, j + offset - processedBytes, processedBytes);
        return sb.toString();
    }
}
