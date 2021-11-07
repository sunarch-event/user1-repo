package com.performance.domain.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.performance.domain.dao.UserDao;
import com.performance.domain.entity.UserMaster;

@Service
public class PerformanceService {

    final static Logger log = LogManager.getLogger(PerformanceService.class);

    private final String MEASURE_FLAG_ON  = "1";

    private final Pattern pattern = Pattern.compile(".新潟県,上越市.");

    private GoogleApiService googleService;

    private UserDao userDao;
    
    private Map<String, Long> resultMap = new HashMap<String, Long>();
    private Map<String, Boolean> assertionResultMap = new HashMap<String, Boolean>();

    public PerformanceService(GoogleApiService googleService, UserDao userDao) {
        this.googleService = googleService;
        this.userDao = userDao;
    }

    @Async("perfomanceExecutor")
    public void execute(String uuid, String measureFlag) {

        resultMap.clear();
        resultMap.put(uuid, null);

        

        Long start = System.currentTimeMillis();

        List<UserMaster> matchingUserList = uploadExecute();

        Long end = System.currentTimeMillis();
        Long executeTime = end - start;

        resultMap.put(uuid, executeTime);
        // アサーション入れる
        Boolean assertionResult = assertion(matchingUserList);
        assertionResultMap.put(uuid, assertionResult);
        
        // 計測実施かつアサーションが成功している場合のみ送る
        if(MEASURE_FLAG_ON.equals(measureFlag) && assertionResult) {
            try {
                googleService.execute(executeTime);
            } catch (Exception e) {
                log.error("スプレッドシートの更新でエラーが発生しました。", e);
            }
        }
        return;
    }
    public List<UserMaster> uploadExecute() {
        // テーブル情報を空にする
        /** 変更不可 **/
        truncateTable();
        /** 変更不可 **/

        dropIndex();

        // CSVを取得・CSVファイルをDBに登録する
        //ファイル読み込みで使用する3つのクラス
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        List<String> csvFile = new ArrayList<String>();
        try {

            //読み込みファイルのインスタンス生成
            //ファイル名を指定する
            fis = new FileInputStream(new File("data/userInfo.csv"));
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);


            //読み込み行
            String readLine;

            //読み込み行数の管理
            int i = 0;

            //1行ずつ読み込みを行う
            while ((readLine = br.readLine()) != null) {
                i++;
                //データ内容をコンソールに表示する
                log.info("-------------------------------");

                //データ件数を表示
                log.info("データ読み込み" + i + "件目");

                csvFile.add(readLine);
            }
        } catch (Exception e) {
            log.error("csv read error", e);
        } finally {
            try {
                br.close();
                isr.close();
                fis.close();
            } catch (Exception e) {
            }
        }

        try {
            int j = 0;
            List<UserMaster> insertUserMasterList = new ArrayList<UserMaster>();
            for (String line : csvFile) {
                //カンマで分割した内容を配列に格納する
                String[] data = line.split(",", -1);

                //データ内容をコンソールに表示する
                log.info("-------------------------------");
                //データ件数を表示
                //配列の中身を順位表示する。列数(=列名を格納した配列の要素数)分繰り返す
                log.debug("ユーザー姓:" + data[1]);
                log.debug("出身都道府県:" + data[2]);
                log.debug("ユーザー名:" + data[0]);
                log.debug("出身市区町村:" + data[3]);
                log.debug("血液型:" + data[4]);
                log.debug("趣味1:" + data[5]);
                log.debug("趣味2:" + data[6]);
                log.debug("趣味3:" + data[7]);
                log.debug("趣味4:" + data[8]);
                log.debug("趣味5:" + data[9]);

                // 特定の件のみインサートするようにする
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    // 行数のインクリメント
                    j++;
                    log.info("データ書き込み" + j + "件目");
                    insertUserMasterList.add(
                        new UserMaster(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]));
                }
            }
            userDao.insertUserInfoAndUserHobby(insertUserMasterList);
        } catch (Exception e) {
            log.error("csv read error", e);
        }
        // 対象情報取得
        createIndex();
        UserMaster targetUserMaster = userDao.getTargetUserMaster();

        // DBから検索する
        dropIndex();
        return userDao.searchUserMaster(targetUserMaster);
    }

    
    public void truncateTable() {
        userDao.truncateUserInfo();
        userDao.truncateUserHobby();
    }

    public void dropIndex() {
        userDao.dropIndex();
    }

    public void createIndex() {
        userDao.createIndex();
    }

    public Long referenceExecuteTime(String uuid) {
        
        Long result = null;
        if(resultMap.containsKey(uuid)) {
            result = resultMap.get(uuid);
        }
        
        return result;
    }
    
    public String referenceUuid() {
        
        String uuid = null;
        
        for(String key : resultMap.keySet()) {
            uuid = key;
        }
        
        return uuid;
    }

    private Boolean assertion(List<UserMaster> matchingUserList) {
        Boolean assertionResult = true;
        
        int count = userDao.searchCount();
        
        if(count != 10000) {
            return false;
        }
        
        if(matchingUserList.size() != 2072) {
            return false;
        }
        
        // CSVを取得・CSVファイルをDBに登録する
        //ファイル読み込みで使用する3つのクラス
        FileReader fr = null;
        BufferedReader br = null;
        List<String> csvFile = new ArrayList<String>();
        try {

            //読み込みファイルのインスタンス生成
            //ファイル名を指定する
            fr = new FileReader(new File("data/assertionData.csv"));
            br = new BufferedReader(fr);

            //読み込み行
            String readLine;
            //1行ずつ読み込みを行う
            while ((readLine = br.readLine()) != null) {
                csvFile.add(readLine);
            }
        } catch (Exception e) {
            log.error("csv read error", e);
        } finally {
            try {
                br.close();
            } catch (Exception e) {
            }
        }
        for(String line : csvFile) {
            boolean exsits = false;
            UserMaster userMaster = new UserMaster();
            String[] data = line.split(",", -1);

            userMaster.setLastName(data[0]);
            userMaster.setFirstName(data[1]);
            userMaster.setPrefectures(data[2]);
            userMaster.setCity(data[3]);
            userMaster.setBloodType(data[4]);
            userMaster.setHobby1(data[5]);
            userMaster.setHobby2(data[6]);
            userMaster.setHobby3(data[7]);
            userMaster.setHobby4(data[8]);
            userMaster.setHobby5(data[9]);
            for(UserMaster user : matchingUserList) {
                if(user.toString().equals(userMaster.toString())) {
                    exsits = true;
                    break;
                }
            }
            if(!exsits) {
                assertionResult = false;
            }
        }
        truncateTable();
        return assertionResult;
    }

    public Boolean referenceAssertionResult(String uuid) {
        Boolean assertionResult = assertionResultMap.get(uuid);
        return assertionResult;
    }
}
