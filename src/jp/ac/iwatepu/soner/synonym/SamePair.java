package jp.ac.iwatepu.soner.synonym;

public class SamePair {
	public int id1;
	public int id2;
	public SamePair(int id1, int id2) {
		super();
		if (id1 >= id2) {
			this.id1 = id1;
			this.id2 = id2;	
		} else {
			this.id1 = id2;
			this.id2 = id1;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id1;
		result = prime * result + id2;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SamePair other = (SamePair) obj;
		if (id1 != other.id1)
			return false;
		if (id2 != other.id2)
			return false;
		return true;
	}
	
	
}
