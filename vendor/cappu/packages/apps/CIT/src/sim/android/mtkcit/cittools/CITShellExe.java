package sim.android.mtkcit.cittools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.util.Log;

public class CITShellExe {
	public static String ERROR = "ERROR";
	private static StringBuilder sb = new StringBuilder("");
	private static boolean debugflag = true;
	private final static String TAG = "CITShellExe";

	/**
	 * get the message after execute
	 * 
	 * @return
	 */
	public static String getOutput() {
		return sb.toString();
	}

	public static int execWriteCommand(String commamd) throws IOException {

		Runtime runtime = Runtime.getRuntime();
		runtime.exec(commamd);
		return 1;
	}

	/**
	 * help to execute the command in shell
	 * 
	 * @param commamd
	 * @return 0 is ture and -1 is fail
	 * @throws IOException
	 */
	public static int execCommand(String[] command) throws IOException {
		Log.v(TAG, command[0] + command[1] + command[2]); // return2

		// start the ls command running
		// String[] args = new String[]{"sh", "-c", command};
		Runtime runtime = Runtime.getRuntime();
		Process proc = runtime.exec(command);
		InputStream inputstream = proc.getInputStream();
		InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
		BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
		sb.delete(0, sb.length());
		try {
			if (proc.waitFor() != 0) {
				CITTools.LOGV(debugflag, TAG,
						"exit value = " + proc.exitValue());
				sb.append(ERROR);
				return -1;
			} else {
				String line;
				line = bufferedreader.readLine();
				if (line != null) {
					sb.append(line);
				} else {
					return 0;
				}
				while (true) {
					line = bufferedreader.readLine();
					if (line == null) {
						break;
					} else {
						sb.append('\n');
						sb.append(line);
					}
				}
				return 0;
			}
		} catch (InterruptedException e) {
			CITTools.LOGV(debugflag, TAG, "exe fail " + e.toString());
			sb.append(ERROR);
			return -1;
		}
	}
}
