/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.silverstatistics.control;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.util.StringUtil;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

/**
 * Class declaration Get cumul datas from database to access and Volume
 */
public class SilverStatisticsPeasDAOAccesVolume {

  private static final String SELECT_VOLUME_YEARS =
      "SELECT DISTINCT dateStat " + "FROM sb_stat_volumecumul ORDER BY dateStat ASC";
  private static final String SELECT_VOLUME_BY_USER = "SELECT componentId, SUM(countVolume) " +
      "AS volume FROM sb_stat_volumecumul WHERE datestat= ? AND userId = ? GROUP BY " +
      "datestat, componentId ORDER BY datestat ASC, volume DESC";
  private static final String SELECT_VOLUME_FOR_ALL_COMPONENTS = "SELECT componentId, " +
      "SUM(countVolume) AS volume FROM SB_Stat_VolumeCumul WHERE dateStat = ? " +
      "GROUP BY dateStat, componentId ORDER BY dateStat ASC, volume DESC";
  private static final String SELECT_ACCESS_YEARS =
      "SELECT DISTINCT dateStat " + "FROM sb_stat_accesscumul ORDER BY dateStat ASC";
  private static final String SELECT_ACCESS_EVOL_FOR_SPACE =
      "SELECT dateStat, SUM(countAccess) AS " +
          "accesses FROM sb_stat_accessCumul WHERE spaceId = ? GROUP BY dateStat ORDER BY " +
          "dateStat ASC";
  private static final String SELECT_ACCESS_EVOL_FOR_COMPONENT =
      "SELECT dateStat, SUM(countAccess) " +
          "AS accesses FROM sb_stat_accessCumul WHERE componentId = ? GROUP BY dateStat " +
          "ORDER BY dateStat ASC";
  private static final String SELECT_ACCESS_EVOL_FOR_SPACE_BY_USER = "SELECT dateStat, " +
      "SUM(countAccess) AS accesses FROM sb_stat_accessCumul WHERE spaceId = ? AND userId = ? " +
      "GROUP BY dateStat ORDER BY dateStat ASC";
  private static final String SELECT_ACCESS_EVOL_FOR_COMPONENT_BY_USER = "SELECT dateStat, " +
      "SUM(countAccess) AS accesses FROM sb_stat_accessCumul WHERE componentId = ? " +
      "AND userId = ? GROUP BY dateStat ORDER BY dateStat ASC";
  private static final String SELECT_ACCESS_FOR_ALL_COMPONENTS = "SELECT componentId, " +
      "SUM(countAccess) AS accesses FROM sb_stat_accesscumul WHERE datestat=? GROUP BY " +
      "dateStat, componentId ORDER BY dateStat ASC, accesses DESC";
  private static final String SELECT_ACCESS_FOR_USER = "SELECT componentId, SUM(countAccess) AS " +
      "accesses  FROM sb_stat_accesscumul WHERE dateStat= ? AND userId = ? GROUP BY dateStat, " +
      "componentId ORDER BY dateStat ASC, accesses DESC";
  private static final String SELECT_ACCESS_COMPONENTS_USER =
      "(SELECT componentId, countAccess, datestat " + "FROM sb_stat_access " +
          "WHERE userId = ? ) " + "UNION " + "(SELECT componentId, countAccess, datestat " +
          "FROM sb_stat_accesscumul " + "WHERE userId = ? ) " +
          "ORDER BY dateStat DESC, countAccess DESC";
  private static final String COMPONENT_ID = "componentId";

  private SilverStatisticsPeasDAOAccesVolume() {

  }

  public static Collection<String> getVolumeYears() throws SQLException {
    return getYearsWithQuery(SELECT_VOLUME_YEARS);
  }

  @Nonnull
  private static Collection<String> getYearsWithQuery(final String selectQuery)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.openConnection();
      stmt = myCon.prepareStatement(selectQuery);
      rs = stmt.executeQuery();
      LinkedHashSet<String> years = new LinkedHashSet<>();
      while (rs.next()) {
        String currentYear = extractYearFromDate(rs.getString("dateStat"));
        years.add(currentYear);
      }
      return years;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  public static Collection<String> getAccessYears() throws SQLException {
    return getYearsWithQuery(SELECT_ACCESS_YEARS);
  }

  private static String extractYearFromDate(String date) {
    return date.substring(0, 4);
  }

  public static Map<String, String[]> getStatsUserVentil(String dateStat, String filterIdGroup,
      String filterIdUser) throws SQLException {
    Map<String, String[]> resultat = new HashMap<>();
    Map<String, String> hashTout = selectAccessForAllComponents(dateStat);
    filterVisibleComponents(resultat, hashTout);

    // Query Groupe
    if (StringUtil.isDefined(filterIdGroup)) {
      filterById(resultat, 1, selectAccessForGroup(dateStat, filterIdGroup));
    }

    // Query User
    if (StringUtil.isDefined(filterIdUser)) {

      filterById(resultat, 2, selectAccessForUser(dateStat, filterIdUser));
    }

    return resultat;
  }

  public static List<String[]> getStatsUserEvolution(String entite, String entiteId,
      String filterIdGroup, String filterIdUser) throws SQLException, ParseException {
    if ("SPACE".equals(entite)) {
      if (StringUtil.isDefined(filterIdUser)) {
        return selectUserAccessEvolutionForSpace(entiteId, filterIdUser);
      }
      if (StringUtil.isDefined(filterIdGroup)) {
        return selectGroupAccessEvolutionForSpace(entiteId, filterIdGroup);
      }
      return selectAccessEvolutionForSpace(entiteId);
    }
    if (StringUtil.isDefined(filterIdUser)) {
      return selectUserAccessEvolutionForComponent(entiteId, filterIdUser);
    }
    if (StringUtil.isDefined(filterIdGroup)) {
      return selectGroupAccessEvolutionForComponent(entiteId, filterIdGroup);
    }
    return selectAccessEvolutionForComponent(entiteId);
  }

  public static List<String[]> selectAccessEvolutionForSpace(String spaceId)
      throws SQLException, ParseException {
    return getAccessEvolFor(spaceId, SELECT_ACCESS_EVOL_FOR_SPACE);
  }

  @Nonnull
  private static List<String[]> getAccessEvolFor(final String spaceId,
      final String accessQuery) throws SQLException, ParseException {
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.openConnection();
      pstmt = myCon.prepareStatement(accessQuery);
      pstmt.setString(1, spaceId);
      rs = pstmt.executeQuery();
      return getStatsUserFromResultSet(rs);
    } finally {
      DBUtil.close(rs, pstmt);
      DBUtil.close(myCon);
    }
  }

  public static List<String[]> selectUserAccessEvolutionForSpace(String spaceId, String userId)
      throws SQLException, ParseException {
    return getAccessEvolByUser(spaceId, userId, SELECT_ACCESS_EVOL_FOR_SPACE_BY_USER);
  }

  @Nonnull
  private static List<String[]> getAccessEvolByUser(final String spaceId, final String userId,
      final String accessEvolQuery) throws SQLException, ParseException {
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.openConnection();
      pstmt = myCon.prepareStatement(accessEvolQuery);
      pstmt.setString(1, spaceId);
      pstmt.setInt(2, Integer.parseInt(userId));
      rs = pstmt.executeQuery();
      return getStatsUserFromResultSet(rs);
    } finally {
      DBUtil.close(rs, pstmt);
      DBUtil.close(myCon);
    }
  }

  public static List<String[]> selectGroupAccessEvolutionForSpace(String spaceId, String groupId)
      throws SQLException, ParseException {
    UserDetail[] users = getAdminController().getAllUsersOfGroup(groupId);
    Map<String, String[]> allAccesses = new HashMap<>();
    for (UserDetail user : users) {
      List<String[]> userStats = selectUserAccessEvolutionForSpace(spaceId, user.getId());
      processAccessEvolStat(allAccesses, userStats);
    }
    return processAccessEvolDate(allAccesses);
  }

  @Nonnull
  private static List<String[]> processAccessEvolDate(final Map<String, String[]> allAccesses) {
    List<String> dates = new ArrayList<>(allAccesses.keySet());
    Collections.sort(dates);
    List<String[]> result = new ArrayList<>(dates.size());
    for (String date : dates) {
      result.add(allAccesses.get(date));
    }
    return result;
  }

  private static void processAccessEvolStat(final Map<String, String[]> allAccesses,
      final List<String[]> userStats) {
    for (String[] stats : userStats) {
      if (allAccesses.containsKey(stats[0])) {
        String[] currentData = allAccesses.get(stats[0]);
        currentData[1] =
            String.valueOf(Integer.parseInt(currentData[1]) + Integer.parseInt(stats[1]));
      } else {
        allAccesses.put(stats[0], stats);
      }
    }
  }

  public static List<String[]> selectAccessEvolutionForComponent(String componentId)
      throws SQLException, ParseException {
    return getAccessEvolFor(componentId, SELECT_ACCESS_EVOL_FOR_COMPONENT);
  }

  public static List<String[]> selectUserAccessEvolutionForComponent(String componentId,
      String userId) throws SQLException, ParseException {
    return getAccessEvolByUser(componentId, userId, SELECT_ACCESS_EVOL_FOR_COMPONENT_BY_USER);
  }

  public static List<String[]> selectGroupAccessEvolutionForComponent(String componentId,
      String groupId) throws SQLException, ParseException {
    UserDetail[] users = getAdminController().getAllUsersOfGroup(groupId);
    Map<String, String[]> allAccesses = new HashMap<>();
    for (UserDetail user : users) {
      List<String[]> userStats = selectUserAccessEvolutionForComponent(componentId, user.getId());
      processAccessEvolStat(allAccesses, userStats);
    }
    return processAccessEvolDate(allAccesses);
  }

  private static List<String[]> getStatsUserFromResultSet(ResultSet rs)
      throws SQLException, ParseException {
    List<String[]> myList = new ArrayList<>();
    Calendar calDateRef = null;

    while (rs.next()) {
      String date = rs.getString(1);
      long count = rs.getLong(2);

      if (calDateRef == null) {// initialisation
        calDateRef = Calendar.getInstance();
        calDateRef.setTime(DateUtil.parseISO8601Date(date));
        calDateRef.set(Calendar.HOUR, 0);
        calDateRef.set(Calendar.MINUTE, 0);
        calDateRef.set(Calendar.SECOND, 0);
        calDateRef.set(Calendar.MILLISECOND, 0);
      }
      Calendar currentDate = Calendar.getInstance();
      currentDate.setTime(DateUtil.parseISO8601Date(date));
      currentDate.set(Calendar.HOUR, 0);
      currentDate.set(Calendar.MINUTE, 0);
      currentDate.set(Calendar.SECOND, 0);
      currentDate.set(Calendar.MILLISECOND, 0);

      while (calDateRef.before(currentDate)) {
        String[] stat = new String[2];
        stat[0] = DateUtil.formatAsISO8601Day(calDateRef.getTime());
        stat[1] = "0";
        myList.add(stat);
        calDateRef.add(Calendar.MONTH, 1);
      }
      String[] stat = new String[2];
      stat[0] = date;
      stat[1] = Long.toString(count);
      myList.add(stat);
      calDateRef.add(Calendar.MONTH, 1);
    }
    return myList;
  }

  static void addNewStatistic(Map<String, String> result, String date, long count) {
    result.put(date, String.valueOf(count));
  }

  public static Map<String, String[]> getStatsPublicationsVentil(String dateStat,
      String filterIdGroup, String filterIdUser) throws SQLException {
    Map<String, String[]> resultat = new HashMap<>();
    Map<String, String> hashTout = selectVolumeForAllComponents(dateStat);
    filterVisibleComponents(resultat, hashTout);

    // Query Group
    if (StringUtil.isDefined(filterIdGroup)) {
      filterById(resultat, 1, selectVolumeForGroup(dateStat, filterIdGroup));
    }

    // Query User
    if (StringUtil.isDefined(filterIdUser)) {
      filterById(resultat, 2, selectVolumeForUser(dateStat, filterIdUser));
    }

    return resultat;
  }

  public static void filterById(final Map<String, String[]> resultat, final int valueIdx,
      final Map<String, String> volume) {
    for (Map.Entry<String, String[]> componentStatistic : resultat.entrySet()) {
      componentStatistic.getValue()[valueIdx] = "0";
    }

    for (Map.Entry<String, String[]> componentStatistic : resultat.entrySet()) {
      if (componentStatistic.getValue() != null) {
        componentStatistic.getValue()[valueIdx] = volume.get(componentStatistic.getKey());
      }
    }
  }

  static Map<String, String> selectVolumeForUser(String dateStat, String filterIdUser)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.openConnection();
      stmt = myCon.prepareStatement(SELECT_VOLUME_BY_USER);
      stmt.setString(1, dateStat);
      stmt.setInt(2, Integer.parseInt(filterIdUser));
      rs = stmt.executeQuery();
      Map<String, String> result = new HashMap<>();
      while (rs.next()) {
        addNewStatistic(result, rs.getString(COMPONENT_ID), rs.getLong("volume"));
      }
      return result;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  static Map<String, String> selectAccessForAllComponents(String dateStat) throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.openConnection();
      stmt = myCon.prepareStatement(SELECT_ACCESS_FOR_ALL_COMPONENTS);
      stmt.setString(1, dateStat);
      rs = stmt.executeQuery();
      Map<String, String> result = new HashMap<>();
      while (rs.next()) {
        addNewStatistic(result, rs.getString(COMPONENT_ID), rs.getLong("accesses"));
      }
      return result;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  static Map<String, String> selectAccessForUser(String dateStat, String filterIdUser)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.openConnection();
      stmt = myCon.prepareStatement(SELECT_ACCESS_FOR_USER);
      stmt.setString(1, dateStat);
      stmt.setInt(2, Integer.parseInt(filterIdUser));
      rs = stmt.executeQuery();
      Map<String, String> result = new HashMap<>();
      while (rs.next()) {
        addNewStatistic(result, rs.getString(COMPONENT_ID), rs.getLong("accesses"));
      }
      return result;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  static Map<String, String> selectAccessForGroup(String dateStat, String groupId)
      throws SQLException {
    UserDetail[] users = getAdminController().getAllUsersOfGroup(groupId);
    Map<String, String> allAccesses = new HashMap<>();
    for (UserDetail user : users) {
      Map<String, String> userStats = selectAccessForUser(dateStat, user.getId());
      for (Map.Entry<String, String> stat : userStats.entrySet()) {
        if (allAccesses.containsKey(stat.getKey())) {
          allAccesses.put(stat.getKey(), String.valueOf(
              Integer.parseInt(allAccesses.get(stat.getKey())) +
                  Integer.parseInt(stat.getValue())));
        } else {
          allAccesses.put(stat.getKey(), stat.getValue());
        }
      }
    }
    return allAccesses;
  }

  static Map<String, String> selectVolumeForGroup(String dateStat, String groupId)
      throws SQLException {
    UserDetail[] users = getAdminController().getAllUsersOfGroup(groupId);
    Map<String, String> allVolumes = new HashMap<>();
    for (UserDetail user : users) {
      Map<String, String> userStats = selectVolumeForUser(dateStat, user.getId());
      for (Map.Entry<String, String> stat : userStats.entrySet()) {
        if (allVolumes.containsKey(stat.getKey())) {
          allVolumes.put(stat.getKey(), String.valueOf(
              Integer.parseInt(allVolumes.get(stat.getKey())) + Integer.parseInt(stat.getValue())));
        } else {
          allVolumes.put(stat.getKey(), stat.getValue());
        }
      }
    }
    return allVolumes;
  }

  static Map<String, String> selectVolumeForAllComponents(String dateStat) throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.openConnection();
      stmt = myCon.prepareStatement(SELECT_VOLUME_FOR_ALL_COMPONENTS);
      stmt.setString(1, dateStat);
      rs = stmt.executeQuery();
      Map<String, String> result = new HashMap<>();
      while (rs.next()) {
        addNewStatistic(result, rs.getString(COMPONENT_ID), rs.getLong("volume"));
      }
      return result;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  static void filterVisibleComponents(Map<String, String[]> resultat,
      Map<String, String> hashTout) {
    for (Map.Entry<String, String> cmp : hashTout.entrySet()) {
      boolean ok = false;
      AdminController myAdminController = getAdminController();
      ComponentInst compInst = myAdminController.getComponentInst(cmp.getKey());
      String spaceId = compInst.getDomainFatherId();
      UserDetail currentUser = UserDetail.getCurrentRequester();
      String[] tabManageableSpaceIds =
          myAdminController.getUserManageableSpaceClientIds(currentUser.getId());
      for (String tabManageableSpaceId : tabManageableSpaceIds) {
        if (spaceId.equals(tabManageableSpaceId)) {
          ok = true;
          break;
        }
      }

      if (ok) {
        String[] values = new String[3];
        values[0] = cmp.getValue();
        resultat.put(cmp.getKey(), values);
      }
    }
  }

  /**
   * Returns the last accessed components of a user.
   * @param currentUserId the identifier of the current user
   * @param nbObjects to return
   * @return the list of the last accessed components of a user.
   * @throws SQLException if an error occurs
   */
  public static Collection<ComponentInstLight> getLastAccessedComponentsUser(String currentUserId,
      int nbObjects) throws SQLException {
    Connection myCon = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Collection<ComponentInstLight> result = new ArrayList<>();
    OrganizationController orgaController =
        OrganizationControllerProvider.getOrganisationController();
    try {
      myCon = DBUtil.openConnection();
      stmt = myCon.prepareStatement(SELECT_ACCESS_COMPONENTS_USER);
      //Setting a cursor to avoid performance problems
      stmt.setFetchSize(50);
      stmt.setInt(1, Integer.parseInt(currentUserId));
      stmt.setInt(2, Integer.parseInt(currentUserId));
      rs = stmt.executeQuery();
      Set<String> performedIds = new HashSet<>(nbObjects * 2);
      while (rs.next() && performedIds.size() < nbObjects) {
        String componentId = rs.getString(COMPONENT_ID);
        //If id is already performed, then it is skipped
        if (orgaController.isComponentExist(componentId) && performedIds.add(componentId)) {
          ComponentInstLight compoDetail = orgaController.getComponentInstLight(componentId);
          if (compoDetail != null) {
            // The compoDetail can be null if componentId references a deleted component
            result.add(compoDetail);
          }
        }
      }
      return result;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  private static AdminController getAdminController() {
    return ServiceProvider.getService(AdminController.class);
  }
}
