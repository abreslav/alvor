package com.zeroturnaround.alvor.cache;

import java.sql.Connection;
import java.sql.SQLException;

public interface IDBLayer {

	Connection connect() throws SQLException, ClassNotFoundException; 
	void shutdown() throws SQLException; 
}
