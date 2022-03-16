package org.mcphackers.mcp.tasks;

import codechicken.diffpatch.cli.DiffOperation;

import org.mcphackers.mcp.MCP;
import org.mcphackers.mcp.MCPPaths;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TaskCreatePatch extends Task {
	public TaskCreatePatch(int side, MCP mcp) {
		super(side, mcp);
	}

	@Override
	public void doTask() throws Exception {
		Path srcPathUnpatched = Paths.get(chooseFromSide(MCPPaths.SRC + "minecraft_unpatched", MCPPaths.SRC + "minecraft_server_unpatched"));
		Path srcPathPatched = Paths.get(chooseFromSide(MCPPaths.CLIENT_SOURCES, MCPPaths.SERVER_SOURCES));
		Path patchesOut = Paths.get(chooseFromSide("patches/patches_client", "patches/patches_server"));
		if (Files.exists(srcPathUnpatched)) {
			if(Files.exists(srcPathPatched)) {
				createDiffOperation(srcPathUnpatched, srcPathPatched, patchesOut);
			}
			else {
				throw new Exception("Patched " + chooseFromSide("client", "server") + " sources cannot be found!");
			}
		} else {
			throw new Exception("Unpatched " + chooseFromSide("client", "server") + " sources cannot be found!");
		}
	}

	public void createDiffOperation(Path aPath, Path bPath, Path outputPath) throws Exception {
		ByteArrayOutputStream logger = new ByteArrayOutputStream();
		DiffOperation diffOperation = DiffOperation.builder()
				.aPath(aPath)
				.bPath(bPath)
				.outputPath(outputPath)
				.verbose(true)
				.logTo(logger)
				.summary(true).build();
		if (diffOperation.operate().exit != 0) {
			// TODO
			//info.addInfo(logger.toString());
			throw new Exception("Patches could not be created!");
		}
	}
}
