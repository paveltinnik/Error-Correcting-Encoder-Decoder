package correcter;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.Scanner;

public class Main {
    static Scanner scanner = new Scanner(System.in);
    static Random random = new Random();
    static DecimalFormat df = new DecimalFormat("00000000");

    static byte[] byteArray;
    static String[] originalHexStringArray;
    static String[] originalBinaryStringArray;
    static String[] binaryStringWithParityArray;
    static String[] binaryStringWithErrorArray;
    static String[] binaryStringDecodedArray;
    static String[] modifiedHexStringArray;

    static StringBuilder originalBinaryString = new StringBuilder();

    /** Read data from file */
    public static void readFromFile(String fileName) {
        try (FileInputStream fis = new FileInputStream(fileName)) {
            byteArray = fis.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Converts initial byteArray to binary String Array */
    public static void toBinary(byte[] array) {
        originalBinaryStringArray = new String[array.length];
        String bin = null;

        // 2's complement if byte is negative
        for (int i = 0; i < array.length; i++) {
            if (array[i] > 0) {
                bin = df.format(Integer.parseInt(Integer.toBinaryString(array[i])));
            } else if (array[i] < 0) {
                StringBuilder invert = new StringBuilder();
                bin = df.format(Integer.parseInt(Integer.toBinaryString((array[i] * -1))));

                for (int j = 0; j < bin.length(); j++) {
                    if (bin.charAt(j) == '0') {
                        invert.append('1');
                    } else {
                        invert.append('0');
                    }
                }
                int sum = Integer.parseInt(invert.toString(), 2) + Integer.parseInt("1", 2);
                bin = df.format(Integer.parseInt(Integer.toBinaryString(sum)));
            } else if (array[i] == 0) {
                bin = "00000000";
            }
            originalBinaryString.append(bin);
            originalBinaryStringArray[i] = bin;
        }
    }

    /** Convert byte array to String hex array */
    public static void toHex(String[] array) {
        originalHexStringArray = new String[array.length];

        for (int i = 0; i < array.length; i++) {
            String hex = Integer.toHexString(Integer.parseInt(array[i], 2)).toUpperCase();
            originalHexStringArray[i] = hex;
        }
    }

    /** Print all elements of an array of type String */
    public static void printElementsOfArray(String[] array, String type) {
        System.out.print(type + " view: ");
        try {
            if (type.equals("text")) {
                byte[] printArray = new byte[array.length];
                for (int i = 0; i < array.length; i++) {
                    printArray[i] = (byte) Integer.parseInt(array[i], 2);
                }
                for (byte bit : printArray) {
                    System.out.print((char) bit);
                }
            } else {
                for (String str : array) {
                    System.out.print(str + " ");
                }
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("null");
        }
    }

    /** Make each bit double and adds a bit of parity */
    public static void makeBinaryWithParity(StringBuilder str) {
        // Create binaryStringWithParityArray with length of str/4
        binaryStringWithParityArray = new String[str.length() / 4];
        modifiedHexStringArray = new String[str.length() / 4];

        for (int i = 0; i < str.length() / 4; i++) {
            StringBuilder sb = new StringBuilder();
            String s = str.substring(i * 4, i * 4 + 4);
            int sum1 = s.charAt(0) + s.charAt(1) + s.charAt(3);
            int sum2 = s.charAt(0) + s.charAt(2) + s.charAt(3);
            int sum3 = s.charAt(1) + s.charAt(2) + s.charAt(3);

            // First parity bit
            if (sum1 % 2 == 0) {
                sb.append('0');
            } else {
                sb.append('1');
            }

            // Second parity bit
            if (sum2 % 2 == 0) {
                sb.append('0');
            } else {
                sb.append('1');
            }

            // Third bit
            sb.append(s.charAt(0));

            // Fourth parity bit
            if (sum3 % 2 == 0) {
                sb.append('0');
            } else {
                sb.append('1');
            }

            // Fifth, sixth and seventh bits
            sb.append(s.substring(1)).append('0');

            binaryStringWithParityArray[i] = sb.toString();
            modifiedHexStringArray[i] = Integer.toHexString(Integer.parseInt(sb.toString(), 2)).toUpperCase();
        }
    }

    /** Make a mistake on one bit of each byte */
    public static void makeErrorInBinaryStringArray(String[] array) {
        binaryStringWithErrorArray = new String[array.length];
        modifiedHexStringArray = new String[array.length];

        for (int i = 0; i < array.length; i++) {
            StringBuilder stb = new StringBuilder(array[i]);
            int at = random.nextInt(8);

            if (stb.charAt(at) == '1') {
                stb.replace(at, at + 1, "0");
            } else {
                stb.replace(at, at + 1, "1");
            }
            binaryStringWithErrorArray[i] = stb.toString();
            modifiedHexStringArray[i] = Integer.toHexString(Integer.parseInt(stb.toString(), 2)).toUpperCase();
        }
    }

    /** Write data bytes to file */
    public static void writeToFile(String fileName, String[] array) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            byte[] write = new byte[array.length];
            for (int i = 0; i < array.length; i++) {
                write[i] = (byte) Integer.parseInt(array[i], 2);
            }
            fos.write(write);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Decode data*/
    public static void decode(String[] array) {
        StringBuilder finish = new StringBuilder();
        binaryStringDecodedArray = new String[array.length / 2];
        modifiedHexStringArray = new String[array.length / 2];

        for (String s : array) {
            StringBuilder str = new StringBuilder(s);

            char b0 = str.charAt(0);
            char b1 = str.charAt(1);
            char b2 = str.charAt(2);
            char b3 = str.charAt(3);
            char b4 = str.charAt(4);
            char b5 = str.charAt(5);
            char b6 = str.charAt(6);

            boolean p1 = (b2 ^ b4 ^ b6) == b0;
            boolean p2 = (b2 ^ b5 ^ b6) == b1;
            boolean p3 = (b4 ^ b5 ^ b6) == b3;

            // Replace incorrect bit
            int pos = (!p1 ? 1 : 0) + (!p2 ? 2 : 0) + (!p3 ? 4 : 0) - 1;

            if (pos == 2) {
                finish.append((char) (b2 ^ 1)).append(b4).append(b5).append(b6);
            } else if (pos == 4) {
                finish.append(b2).append((char) (b4 ^ 1)).append(b5).append(b6);
            } else if (pos == 5) {
                finish.append(b2).append(b4).append((char) (b5 ^ 1)).append(b6);
            } else if (pos == 6) {
                finish.append(b2).append(b4).append(b5).append((char) (b6 ^ 1));
            } else {
                finish.append(b2).append(b4).append(b5).append(b6);
            }
        }

        for (int i = 0; i < finish.length() / 8; i++) {
            String write = finish.substring(i*8, i*8 + 8);
            binaryStringDecodedArray[i] = write;
            modifiedHexStringArray[i] = Integer.toHexString(Integer.parseInt(write, 2)).toUpperCase();
        }
    }

    /** Show start menu */
    public static void showStartMenu() {
        System.out.print("Write a mode: ");
        String mode = scanner.next();
        switch (mode) {
            case "encode":
                Encode.encode();
                break;
            case "decode":
                Decode.decode();
                break;
            case "send":
                Send.send();
                break;
        }
    }

    public static void main(String[] args) {
        showStartMenu();
    }
}

class Encode extends Main {
    public static void encode() {
        // Read data from file
        readFromFile("src\\main\\java\\correcter\\send.txt");

        // Make binary representation of byteArray and print binary elements
        toBinary(byteArray); // -> originalBinaryStringArray
        toHex(originalBinaryStringArray); // -> originalHexStringArray

        // Print text and hex and binary presentation
        System.out.println("send.txt:");
        printElementsOfArray(originalBinaryStringArray, "text");
        printElementsOfArray(originalHexStringArray, "hex");
        printElementsOfArray(originalBinaryStringArray, "bin");

        // Make original string with parity
        makeBinaryWithParity(originalBinaryString); // -> binaryStringWithParityArray

        // Print parity and hex presentation
        System.out.println("\nencoded.txt:");
        printElementsOfArray(binaryStringWithParityArray, "parity");
        printElementsOfArray(modifiedHexStringArray, "hex");

        // Write bytes to file
        writeToFile("encoded.txt", binaryStringWithParityArray);
    }
}

class Send extends Main {
    public static void send() {
        readFromFile("encoded.txt");

        // Make binary representation of byteArray and print binary elements
        toBinary(byteArray); // -> originalBinaryStringArray
        toHex(originalBinaryStringArray); // -> originalHexStringArray

        // Print binary and hex presentation
        System.out.println("encoded.txt:");
        printElementsOfArray(originalHexStringArray, "hex");
        printElementsOfArray(originalBinaryStringArray, "bin");

        // Make errors in originalBinaryStringArray and write its elements to binaryStringWithErrorArray
        makeErrorInBinaryStringArray(originalBinaryStringArray); // -> binaryStringWithErrorArray

        // Print binary and hex presentation
        System.out.println("\nreceived.txt:");
        printElementsOfArray(modifiedHexStringArray, "hex");
        printElementsOfArray(binaryStringWithErrorArray, "bin");

        // Write bytes to file
        writeToFile("received.txt", binaryStringWithErrorArray);
    }
}

class Decode extends Main {
    public static void decode() {
        readFromFile("received.txt");

        // Make binary representation of byteArray and print binary elements
        toBinary(byteArray); // -> originalBinaryStringArray
        toHex(originalBinaryStringArray); // -> originalHexStringArray

        // Print binary and hex presentation
        System.out.println("received.txt:");
        printElementsOfArray(originalHexStringArray, "hex");
        printElementsOfArray(originalBinaryStringArray, "bin");

        // Decode reading data
        decode(originalBinaryStringArray);

        // Print binary and hex presentation
        System.out.println("\ndecoded.txt:");
        printElementsOfArray(modifiedHexStringArray, "hex");
        printElementsOfArray(binaryStringDecodedArray, "bin");
        printElementsOfArray(binaryStringDecodedArray, "text");

        // Write bytes to file
        writeToFile("decoded.txt", binaryStringDecodedArray);
    }
}