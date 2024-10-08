package nro.models.boss.mapoffline;

import java.util.ArrayList;
import java.util.List;
import lombok.Setter;
import nro.consts.ConstItem;
import nro.consts.ConstMap;
import nro.consts.ConstPlayer;
import nro.consts.ConstRatio;
import nro.consts.ConstTask;
import nro.event.Event;
import nro.lib.RandomCollection;
import nro.models.boss.Boss;
import nro.models.boss.mabu_war.*;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.boss.BossManager;
import nro.models.boss.FutureBoss;
import nro.models.boss.cdrd.CBoss;
import nro.models.boss.iboss.BossInterface;
import nro.models.boss.nappa.Kuku;
import nro.models.boss.nappa.MapDauDinh;
import nro.models.boss.nappa.Rambo;
import nro.models.item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.npc.NpcFactory;
import nro.models.player.Player;
import nro.models.skill.Skill;
import nro.server.Manager;
import nro.server.ServerNotify;
import nro.services.MapService;
import nro.services.PlayerService;
import nro.services.RewardService;
import nro.services.Service;
import nro.services.SkillService;
import nro.services.TaskService;
import nro.services.func.ChangeMapService;
import nro.utils.Log;
import nro.utils.SkillUtil;
import nro.utils.Util;

/**
 * @author outcast c-cute hột me 😳
 */
/**
 * @copyright 💖 GirlkuN 💖
 */
public class Boss_ThanMeo extends FutureBoss {

    public Boss_ThanMeo(int bossID, BossData bossData, Zone zone, int x, int y, int id_player) throws Exception {
        super(bossID - 100000, bossData);
        this.zone = zone;
        this.location.x = x;
        this.location.y = y;
        this.idPlayerForNPC = id_player;
    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }

    @Override
    public void rewards(Player pl) {
        if (pl.playerTask.taskMain.id == 10 && pl.playerTask.taskMain.index == 0 && pl.thachDauNPC == 1) {
            pl.playerTask.taskMain.index++;
            TaskService.gI().sendTaskMain(pl);
            pl.thachDauNPC = 1;
        }else if (pl.thachDauNPC == 1){
            pl.doneThachDauThanMeo = 1;
        }
        PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.NON_PK);
    }

    @Override
    public void idle() {
    }

    @Override
    public void checkPlayerDie(Player pl) {
        if (pl.isDie()) {
            goToXY(pl.location.x + 2, pl.location.y, false);
            goToXY(pl.location.x - 2, pl.location.y, false);
            this.chat("Luyện tập thêm đi");
            try {
                Thread.sleep(5000);
                leaveMap();
                hide_npc(pl, 18, 1);
            } catch (InterruptedException ex) {
                System.out.println("checkPlayerDie_" + name);
            }
        }
    }

    @Override
    public void attack() {
        try {
            if (this.zone == null) {
                leaveMap();
            } else {
                Player pl = super.zone.findPlayerByID(super.idPlayerForNPC);
                if (pl != null && !pl.isDie() && !pl.isMiniPet) {
                    PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.PK_PVP);
                    PlayerService.gI().changeAndSendTypePK(pl, ConstPlayer.PK_PVP);
                    this.playerSkill.skillSelect = this.getSkillAttack();

                    // Kiểm tra null cho playerSkill và playerSkill.skillSelect
                    if (this.playerSkill != null && this.playerSkill.skillSelect != null) {

                        double distance = Util.getDistance(this, pl);
                        double range = this.getRangeCanAttackWithSkillSelect();

                        // Kiểm tra distance và range
                        if (distance <= range) {
                            if (Util.isTrue(15, ConstRatio.PER100)) {
                                if (SkillUtil.isUseSkillChuong(this)) {
                                    goToXY(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 80)),
                                            Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 50), false);
                                } else {
                                    goToXY(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 30)),
                                            Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 50), false);
                                }
                            }
                            SkillService skillService = SkillService.gI();

                            // Kiểm tra null cho skillService
                            if (skillService != null) {
                                skillService.useSkill(this, pl, null, null);
                                checkPlayerDie(pl);
                            } else {
                                // Xử lý khi skillService là null
                                Log.error("SkillService is null");
                            }
                        } else {
                            goToPlayer(pl, false);
                        }
                    } else {
                        // Xử lý khi playerSkill hoặc playerSkill.skillSelect là null
                        Log.error("playerSkill or playerSkill.skillSelect is null");
                    }
                } else {
                    leaveMap();
                }
            }
        } catch (Exception ex) {
            Log.error(Boss.class, ex);
        }
    }

    @Override
    public int injured(Player plAtt, int damage, boolean piercing, boolean isMobAttack) {
        if (plAtt != null && plAtt.id == super.idPlayerForNPC) {
            int dame = 0;
            if (this.isDie()) {
                return dame;
            } else {
                if (Util.isTrue(1, 5) && plAtt != null) {
                    switch (plAtt.playerSkill.skillSelect.template.id) {
                        case Skill.TU_SAT:
                        case Skill.QUA_CAU_KENH_KHI:
                        case Skill.MAKANKOSAPPO:
                            break;
                        default:
                            return 0;
                    }
                }

                dame = super.injured(plAtt, damage, piercing, isMobAttack);
                if (dame > 199) {
                    dame = 199;
                }
                if (this.isDie()) {
                    rewards(plAtt);
                    notifyPlayeKill(plAtt);
                    PlayerService.gI().changeAndSendTypePK(plAtt, ConstPlayer.NON_PK);
                    die();
                }
                return dame;
            }
        }
        return 0;
    }

    protected void notifyPlayeKill(Player player) {
        if (player != null) {
        }
    }

    @Override
    public void initTalk() {
        this.textTalkBefore = new String[]{"Ta sẽ dạy ngươi vài chiêu"};
        this.textTalkMidle = new String[]{"Haizzzzz", "Xem đây", "Hahaha", "ai da"};
        this.textTalkAfter = new String[]{"OK ta chịu thua"};
    }

    @Override
    public void leaveMap() {
        super.leaveMap();
    }
}
