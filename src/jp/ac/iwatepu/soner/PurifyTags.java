package jp.ac.iwatepu.soner;

import java.sql.SQLException;

public class PurifyTags {
	public static void main(String[] args) throws SQLException {
		//FIXME: remove? merge with some other module?
		DBConnector.getInstance().createPurifiedAttributes();
	}
}
