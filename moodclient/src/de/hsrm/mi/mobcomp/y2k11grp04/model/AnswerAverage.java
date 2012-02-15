package de.hsrm.mi.mobcomp.y2k11grp04.model;

import android.os.Parcel;
import android.os.Parcelable;

public class AnswerAverage extends BaseModel {
	private Question question;
	private int average;
	private String answer;
	private int numVotes;

	public AnswerAverage() {
	}

	public AnswerAverage(Parcel in) {
		readFromParcel(in);
	}

	@Override
	protected void readFromParcel(Parcel in) {
		super.readFromParcel(in);
		average = in.readInt();
		numVotes = in.readInt();
		answer = in.readString();
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeInt(average);
		out.writeInt(numVotes);
		out.writeString(answer);
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public static final Parcelable.Creator<AnswerAverage> CREATOR = new Parcelable.Creator<AnswerAverage>() {
		public AnswerAverage createFromParcel(Parcel in) {
			return new AnswerAverage(in);
		}

		public AnswerAverage[] newArray(int size) {
			return new AnswerAverage[size];
		}
	};

	public int getAverage() {
		return average;
	}

	public void setAverage(int average) {
		this.average = average;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String value) {
		this.answer = value;
	}

	public int getNumVotes() {
		return numVotes;
	}

	public void setNumVotes(int numVotes) {
		this.numVotes = numVotes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((answer == null) ? 0 : answer.hashCode());
		result = prime * result + average;
		result = prime * result + numVotes;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnswerAverage other = (AnswerAverage) obj;
		if (answer == null) {
			if (other.answer != null)
				return false;
		} else if (!answer.equals(other.answer))
			return false;
		if (average != other.average)
			return false;
		if (numVotes != other.numVotes)
			return false;
		if (question == null) {
			if (other.question != null)
				return false;
		} else if (!question.equals(other.question))
			return false;
		return true;
	}
}