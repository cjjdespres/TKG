/*******************************************************************************
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package org.testKitGen;

public class MainRunner {

	private MainRunner() {
	}

	public static void main(String[] args) {
		Arguments argInfo = Arguments.getInstance();
		argInfo.parse(args);
		BuildList bl = new BuildList(argInfo);
		if (argInfo.getMode() == Arguments.Mode.CLEAN) {
			clean(argInfo, bl);
		} else {
			TestTarget tt = TestTarget.getInstance();
			tt.parse(argInfo.getTestTargetName(), argInfo.getTestList());
			ModesDictionary md = new ModesDictionary(argInfo);
			md.parse();
			if (argInfo.getMode() == Arguments.Mode.GEN_TESTS) {
				genTests(argInfo, md, tt, bl);
			} else if (argInfo.getMode() == Arguments.Mode.GEN_PARALLEL_LIST) {
				genParallelList(argInfo, md, tt, bl);
			} else if (argInfo.getMode() == Arguments.Mode.GEN_BUILD_LIST) {
				genBuildList(argInfo, md, tt, bl);
			}
		}
	}

	public static void genTests(Arguments argInfo, ModesDictionary md, TestTarget tt, BuildList bl) {
		System.out.println("Starting to generate test make files.\n");
		DirectoryWalker dw = new DirectoryWalker(argInfo, new TestGenVisitor(argInfo, md, tt), bl);
		dw.traverse();
		if (!tt.getTestSet().isEmpty()) {
			System.out.println("Warning: cannot find the following tests " + tt.getTestSet().toString().replaceAll("\\s+","") + " in TESTLIST\n");
		}
		UtilsGen ug = new UtilsGen(argInfo, md);
		ug.generateFile();
		System.out.println("Make files are generated successfully.\n");
	}

	public static void genParallelList(Arguments argInfo, ModesDictionary md, TestTarget tt, BuildList bl) {
		System.out.println("\nStarting to generate parallel test lists.\n");
		DirectoryWalker dw = new DirectoryWalker(argInfo, new ParallelGenVisitor(argInfo, md, tt), bl);
		dw.traverse();
		TestDivider td = new TestDivider(argInfo, tt);
		td.generateLists();
	}

	public static void genBuildList(Arguments argInfo, ModesDictionary md, TestTarget tt, BuildList bl) {
		System.out.println("\nStarting to generate build list.\n");
		DirectoryWalker dw = new DirectoryWalker(argInfo, new BuildListGenVisitor(argInfo, md, tt, bl), bl);
		dw.traverse();
		bl.generateList();
	}

	public static void clean(Arguments argInfo, BuildList bl) {
		DirectoryWalker dw = new DirectoryWalker(argInfo, new CleanVisitor(), bl);
		dw.traverse();
		UtilsGen ug = new UtilsGen(argInfo, null);
		ug.clean();
		TestDivider td = new TestDivider(argInfo, null);
		td.clean();
		bl.clean();
		System.out.println("\nGenerated makefiles are successfully removed\n");
	}
}