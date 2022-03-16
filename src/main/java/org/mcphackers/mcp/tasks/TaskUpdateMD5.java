package org.mcphackers.mcp.tasks;

import org.mcphackers.mcp.MCP;
import org.mcphackers.mcp.MCPPaths;
import org.mcphackers.mcp.ProgressListener;
import org.mcphackers.mcp.tools.Util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;

public class TaskUpdateMD5 extends Task {
	public boolean recompile;
	public int progress = 0;
	
	private static final int RECOMPILE = 1;
	private static final int MD5 = 2;

	public TaskUpdateMD5(int side, MCP mcp) {
		super(side, mcp);
		this.recompile = true;
	}

	public TaskUpdateMD5(int side, MCP mcp, ProgressListener listener) {
		super(side, mcp, listener);
	}

	@Override
	public void doTask() throws Exception {
		updateMD5(false);
	}

	public void updateMD5(boolean reobf) throws Exception {
		Path binPath 	= Paths.get(chooseFromSide(MCPPaths.CLIENT_BIN, MCPPaths.SERVER_BIN));
		Path md5 = Paths.get(reobf ? chooseFromSide(MCPPaths.CLIENT_MD5_RO, MCPPaths.SERVER_MD5_RO)
				  				   : chooseFromSide(MCPPaths.CLIENT_MD5, 	 MCPPaths.SERVER_MD5));
		step();
		if(recompile) {
			new TaskRecompile(side, mcp, this).doTask();
		}
		step();
		if (Files.exists(binPath)) {
			BufferedWriter writer = new BufferedWriter(new FileWriter(md5.toFile()));
			progress = 0;
			int total = (int)Files.walk(binPath)
					.parallel()
					.filter(p -> !p.toFile().isDirectory())
					.count();
			Files.walkFileTree(binPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					try {
						String md5_hash = Util.getMD5OfFile(file.toFile());
						String fileName = Paths.get(chooseFromSide(MCPPaths.CLIENT_BIN, MCPPaths.SERVER_BIN)).relativize(file).toString().replace("\\", "/").replace(".class", "");
						writer.append(fileName).append(" ").append(md5_hash).append("\n");
						progress++;
						setProgress(50 + (int)((double)progress/(double)total * 50));
					} catch (NoSuchAlgorithmException ex) {
						ex.printStackTrace();
					}
					return FileVisitResult.CONTINUE;
				}
			});
			writer.close();
		} else {
			throw new IOException(chooseFromSide("Client", "Server") + " classes not found!");
		}
	}
	
	public void setProgress(int progress) {
		switch (step) {
		case RECOMPILE: {
			int percent = (int)((double)progress * 0.50D);
			super.setProgress(0 + percent);
			break;
		}
		default:
			super.setProgress(progress);
			break;
		}
	}

	protected void updateProgress() {
		switch (step) {
		case RECOMPILE:
			setProgress("Recompiling");
			break;
		case MD5:
			setProgress("Updating MD5...", 50);
			break;
		default:
			super.updateProgress();
			break;
		}
	}
}
