package template.wizzard;

import com.flca.mda.codegen.data.ITemplate;
import com.flca.mda.codegen.data.Template;
import com.flca.mda.codegen.data.TemplateMergeStrategy;

public enum TidWizzard {

	WIZZARD_MAIN,
	WIZZARD_DIALOG,
	WIZZARD_PAGE,
	WIZZARD_TEST;
	
	
	// --- generate the templates
	public static ITemplate makeTemplate(TidWizzard aTid)
	{
		String jetfile = C.getJetFile(aTid);
		String targetdir = C.getTargetDir(aTid);
		String classname = C.getClassname(aTid);
		String pck = C.getPackage(aTid);
		String ext = C.getFileExt(aTid);
		String genFqn = C.getGeneratorFqn(aTid);
		Class<?> appliesto[] = C.getAppliesTo(aTid);
		TemplateMergeStrategy strategy = C.getMergeStrategy(aTid);
		int rank = 5; 
		
		return new Template(aTid.name(), jetfile, genFqn, targetdir, 
							pck, classname, ext, null,
							appliesto, strategy, rank);
	}
}
