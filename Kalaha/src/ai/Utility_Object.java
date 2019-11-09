package ai;

public class Utility_Object {

	private int eval_score;
	private int ambo_value;

	public Utility_Object() {
		super();
	}

	public Utility_Object(int eval_score, int ambo_value) {
		super();
		this.eval_score = eval_score;
		this.ambo_value = ambo_value;
	}

	@Override
	public String toString() {
		return "Utility_Object [eval_score=" + eval_score + ", ambo_value=" + ambo_value + "]";
	}

	public int getEval_score() {
		return eval_score;
	}

	public void setEval_score(int eval_score) {
		this.eval_score = eval_score;
	}

	public int getAmbo_value() {
		return ambo_value;
	}

	public void setAmbo_value(int ambo_value) {
		this.ambo_value = ambo_value;
	}

}
