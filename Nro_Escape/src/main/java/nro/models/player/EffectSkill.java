package nro.models.player;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import nro.models.boss.Boss;
import nro.models.mob.Mob;
import nro.services.EffectSkillService;
import nro.services.ItemTimeService;
import nro.services.PlayerService;
import nro.services.Service;
import nro.utils.Util;

/**
 *
 * @author 💖 NgocCoder 💖
 * @copyright 💖 GirlkuN 💖
 *
 */
public class EffectSkill {

    private Player player;

    private Boss boss;

    //thái dương hạ san
    public boolean isStun;
    public long lastTimeStartStun;
    public int timeStun;

    //khiên năng lượng
    public boolean isShielding;
    public long lastTimeShieldUp;
    public int timeShield;

    //biến khỉ
    public boolean isMonkey;
    public byte levelMonkey;
    public long lastTimeUpMonkey;
    public int timeMonkey;
    //vantanz
    public boolean isBienHinh;
    public byte levelBienHinh;
    public long lastTimeUpBienHinh;
    public int timeBienHinh;

    //tái tạo năng lượng
    public boolean isCharging;
    public int countCharging;

    //huýt sáo
    public int tiLeHPHuytSao;
    public long lastTimeHuytSao;

    //thôi miên
    public boolean isThoiMien;
    public long lastTimeThoiMien;
    public int timeThoiMien;

    //trói
    public boolean useTroi;
    public boolean anTroi;
    public long lastTimeTroi;
//    public long lastTimeAnTroi;
    public int timeTroi;
//    public int timeAnTroi;
    public Player plTroi;
    public Player plAnTroi;
    public Mob mobAnTroi;

    //dịch chuyển tức thời
    public boolean isBlindDCTT;
    public long lastTimeBlindDCTT;
    public int timeBlindDCTT;

    //socola
    public boolean isSocola;
    public long lastTimeSocola;
    public int timeSocola;
    
 
    
    public boolean isHoaHinh;
    public byte levelHoaHinh;
    public long lastTimeUpHoaHinh;
    public int timeHoaHinh;

    //ma phong ba
    public boolean isMaPhongBa = false;
    public long lastTimeMaPhongBa;
    public int timeMaPhongBa;

    public long lastTimeSubHP;

    public int countPem1hp;
    public int isTaskHoldMabu;
    public long lastTimeHoldMabu;
    public boolean isHoldMabu;

    public EffectSkill(Player player) {
        this.player = player;
    }

    public void removeSkillEffectWhenDie() {
        if (isMonkey) {
            EffectSkillService.gI().monkeyDown(player);
        }
        if (isBienHinh) {
            EffectSkillService.gI().BienHinhDown(player);
        }
        if (isShielding) {
            EffectSkillService.gI().removeShield(player);
            ItemTimeService.gI().removeItemTime(player, 3784);
        }
        if (useTroi) {
            EffectSkillService.gI().removeUseTroi(this.player);
        }
        if (isStun) {
            EffectSkillService.gI().removeStun(this.player);
        }
        if (isThoiMien) {
            EffectSkillService.gI().removeThoiMien(this.player);
        }
        if (isBlindDCTT) {
            EffectSkillService.gI().removeBlindDCTT(this.player);
        }
    }

    public void update() {
        if (isMonkey && (Util.canDoWithTime(lastTimeUpMonkey, timeMonkey))) {
            EffectSkillService.gI().monkeyDown(player);
        }
        if (isBienHinh && (Util.canDoWithTime(lastTimeUpBienHinh, 300000))) {
            EffectSkillService.gI().BienHinhDown(player);
        }
        
        if (isShielding && (Util.canDoWithTime(lastTimeShieldUp, timeShield))) {
            EffectSkillService.gI().removeShield(player);
        }
        if (useTroi && Util.canDoWithTime(lastTimeTroi, timeTroi)
                || plAnTroi != null && plAnTroi.isDie()
                || useTroi && isHaveEffectSkill()) {
            EffectSkillService.gI().removeUseTroi(this.player);
        }
//        if (anTroi && (Util.canDoWithTime(lastTimeAnTroi, timeAnTroi) || player.isDie())) {
//            EffectSkillService.gI().removeAnTroi(this.player);
//        }
        if (isStun && Util.canDoWithTime(lastTimeStartStun, timeStun)) {
            EffectSkillService.gI().removeStun(this.player);
        }
        if (isThoiMien && (Util.canDoWithTime(lastTimeThoiMien, timeThoiMien))) {
            EffectSkillService.gI().removeThoiMien(this.player);
        }
        if (isBlindDCTT && (Util.canDoWithTime(lastTimeBlindDCTT, timeBlindDCTT))) {
            EffectSkillService.gI().removeBlindDCTT(this.player);
        }
        if (isSocola && (Util.canDoWithTime(lastTimeSocola, timeSocola))) {
            EffectSkillService.gI().removeSocola(this.player);
        }
        if (isMaPhongBa) {
            if (Util.canDoWithTime(lastTimeMaPhongBa, timeMaPhongBa)) {
                EffectSkillService.gI().removeMaPhongBa(this.player);
            }
        }
        if (tiLeHPHuytSao != 0 && Util.canDoWithTime(lastTimeHuytSao, 30000)) {
            EffectSkillService.gI().removeHuytSao(this.player);
        }
        if (isMaPhongBa) {
//            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            for (int i = 0; i < (player.effectSkill.timeMaPhongBa / 1000) + 1; i++) {
                if (Util.canDoWithTime(lastTimeSubHP, 1000)) {
                    try {
                        if (!this.player.isDie()) {
                            synchronized (this.player) {
                            if (player.nPoint.hp >= player.dameMaFuBa) {
                                PlayerService.gI().subHPPlayer(this.player, this.player.dameMaFuBa);
                            } else {
                                player.nPoint.hp = 0;
                                Service.getInstance().charDie(player);
                                EffectSkillService.gI().removeMaPhongBa(player);
                                ItemTimeService.gI().removeItemTime(player, 11175);
//                                executorService.shutdown();
                            }
                            PlayerService.gI().sendInfoHpMpMoney(this.player);
                            Service.getInstance().Send_Info_NV(this.player);
                            }
                        }
                        lastTimeSubHP = System.currentTimeMillis();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public boolean isHaveEffectSkill() {
        return isStun || isBlindDCTT || anTroi || isThoiMien;
    }

    public void dispose() {
        this.player = null;
    }
}
