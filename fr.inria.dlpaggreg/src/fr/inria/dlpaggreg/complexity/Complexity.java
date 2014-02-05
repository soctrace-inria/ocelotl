package fr.inria.dlpaggreg.complexity;

public class Complexity {

	public static double	ln	= Math.log(2);

	public static double divergence(final int size, final double val, final double ent) {
		return val * Math.log(size) / ln - entropyReduction(val, ent);
	}

	public static double divergenceExp(final int size, final double val, final double ent) {
		return (val + 1) * Math.log(size) / ln - entropyReduction(val + 1, ent);
	}

	public static double entropy(final double val) {
		return val * Math.log(val) / ln;
	}

	public static double entropyReduction(final double val, final double ent) {
		if (val > 0)
			return entropy(val) - ent;
		else
			return 0.0;
	}

	public static double entropyReductionExp(final double val, final double ent) {
		if (val > 0)
			return entropy(val + 1) - ent;
		else
			return 0.0;
	}

	public static int sizeReduction(final int size) {
		return size - 1;
	}

}
