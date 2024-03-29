/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package data.access;

import business.Account;
import data.pool.ConnectionPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Class provides methods about friendship of users 
 */
public class FriendDAO extends DataDAO {

    /**
     * @param accountId1 - accountId of user 1
     * @param accountId2 - accountId of user 2
     * @return true if two account are friend
     */
    public static boolean areFriends(int accountId1, int accountId2) {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement preStatement = null;
        try {
            String sqlCode =
                    "SELECT * FROM Friend " +
                    "WHERE accountId1 = ? AND accountId2 = ?";
                                    
            preStatement = connection.prepareStatement(sqlCode);
            if (accountId1 < accountId2) {
                preStatement.setInt(1, accountId1);
                preStatement.setInt(2, accountId2);
            } else {
                preStatement.setInt(1, accountId2);
                preStatement.setInt(2, accountId1);
            }
            ResultSet resultSet = preStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
            else {
                return false;
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return false;
        } finally {
            freeDbResouce(preStatement, null, connection, pool);
        }
    }
    
    /**
     * add friend relation into table Friend
     * @param accountId1 - account of first user
     * @param accountId2 - account of second user
     * @return true if success
     */
    public static boolean addFriends(int accountId1, int accountId2) {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement preStatement = null;
        try {
            String sqlCode = 
                    "INSERT INTO Friend VALUES " +
                    "(?, ?)";
            preStatement = connection.prepareStatement(sqlCode);
            if (accountId1 < accountId2) {
                preStatement.setInt(1, accountId1);
                preStatement.setInt(2, accountId2);
            } else {
                preStatement.setInt(1, accountId2);
                preStatement.setInt(2, accountId1);
            }
            
            int nRows = preStatement.executeUpdate();
            if (nRows > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException sqle)  {
            sqle.printStackTrace();
            return false;
        } finally {
            freeDbResouce(preStatement, null, connection, pool);
        }
    }
    
    /**
     * @return list of account of User's friends
     */
    public static ArrayList<Account> getFriendList(int accountId) {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement preStatement = null;
        try {
            String sqlCode =
                    "SELECT acc.* " +
                    "FROM Account as acc, Friend as fr " +
                    "WHERE (fr.accountId1 = ? AND acc.accountId = fr.accountId2) OR " +
                    "      (fr.accountId2 = ? AND acc.accountId = fr.accountId1)" +
                    "ORDER BY username";
            preStatement = connection.prepareStatement(sqlCode);
            preStatement.setInt(1, accountId);
            preStatement.setInt(2, accountId);
            ResultSet resutSet = preStatement.executeQuery();
            
            ArrayList<Account> friendList = new ArrayList<Account>();
            while (resutSet.next()) {
                Account acc = new Account();
                acc.setBasicInfo(resutSet);
                friendList.add(acc);
            }
            return friendList;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return null;
        } finally {
            freeDbResouce(preStatement, null, connection, pool);
        }
    }
    
    /**
     * delete a friend relationship
     * @param accountId1 - accountId of first person
     * @param accountId2 - accountId of second person
     * @return true if success
     */
    public static boolean deleteFriend(int accountId1, int accountId2) {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement preStatement = null;
        try {
            String sqlCode = 
                    "DELETE FROM Friend " +
                    "WHERE (accountId1 = ? AND accountId2 = ?) OR " +
                    "      (accountId1 = ? AND accountId2 = ?)";
            preStatement = connection.prepareStatement(sqlCode);
            preStatement.setInt(1, accountId1);
            preStatement.setInt(2, accountId2);
            preStatement.setInt(3, accountId2);
            preStatement.setInt(4, accountId1);
            int nRows = preStatement.executeUpdate();
            if (nRows > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return false;
        } finally {
            freeDbResouce(preStatement, null, connection, pool);
        }
    }
    
    /**
     * get number of friends
     * @param accountId - accountId of the account
     */
    public static int getNumOfFriends(int accountId) {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement preStatement = null;
        try {
            String sqlCode = 
                    "SELECT * FROM Friend " +
                    "WHERE accountId1 = ? OR accountId2 = ?";
            preStatement = connection.prepareStatement(sqlCode);
            preStatement.setInt(1, accountId);
            preStatement.setInt(2, accountId);
            ResultSet resultSet = preStatement.executeQuery();
            if (resultSet.last()) {
                return resultSet.getRow();
            } else {
                return 0;
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return 0;
        } finally {
            freeDbResouce(preStatement, null, connection, pool);
        }
    }
    
    /**
     * get number of common friends between two different users
     * @param accountId1 - accountId of the first person
     * @param accountId2 - accountId of the second person
     * @return number of common friends
     */
    public static int getNumOfCommonFriends(int accountId1, int accountId2) {
        int smallId = Math.min(accountId1, accountId2);
        int bigId = Math.max(accountId1, accountId2);
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement preStatement = null;
        try {
            int numOfCommons = 0;
            
            // find common friends that has the accountId less than two users
            String sqlCode =
                    "SELECT * " +
                    "FROM Friend as f1, Friend as f2 " +
                    "WHERE f1.accountId1 = f2.accountId1 AND " +
                    "      f1.accountId2 = ? AND f2.accountId2 = ? ";
            preStatement = connection.prepareStatement(sqlCode);
            preStatement.setInt(1, smallId);
            preStatement.setInt(2, bigId);
            ResultSet resultSet = preStatement.executeQuery();
            if (resultSet.last()) {
                numOfCommons += resultSet.getRow();
            }

            resultSet.close();
            preStatement.close();
            // find common friends that has the accountId between two users' accountId
            sqlCode = 
                    "SELECT * " +
                    "FROM Friend as f1, Friend as f2 " +
                    "WHERE f1.accountId2 = f2.accountId1 AND " +
                    "      f1.accountId1 = ? AND f2.accountId2 = ? ";
            preStatement = connection.prepareStatement(sqlCode);
            preStatement.setInt(1, smallId);
            preStatement.setInt(2, bigId);
            resultSet = preStatement.executeQuery();
            if (resultSet.last()) {
                numOfCommons += resultSet.getRow();
            }
            
            resultSet.close();
            preStatement.close();
            // find common friend that has the accountId greater than two users' accountId
            sqlCode =
                    "SELECT * " +
                    "FROM Friend as f1, Friend as f2 " +
                    "WHERE f1.accountId2 = f2.accountId2 AND " +
                    "      f1.accountId1 = ? AND f2.accountId1 = ? ";
            preStatement = connection.prepareStatement(sqlCode);
            preStatement.setInt(1, smallId);
            preStatement.setInt(2, bigId);
            resultSet = preStatement.executeQuery();
            if (resultSet.last()) {
                numOfCommons += resultSet.getRow();
            }
            resultSet.close();
            return numOfCommons;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return 0;
        } finally {
            freeDbResouce(preStatement, null, connection, pool);
        }
    }
}
