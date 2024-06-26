package nro.services.func;

import nro.consts.ConstNpc;
import nro.jdbc.daos.PlayerDAO;
import nro.models.item.Item;
import nro.models.map.Zone;
import nro.models.npc.Npc;
import nro.models.npc.NpcManager;
import nro.models.player.Player;
import nro.server.Client;
import nro.server.io.Message;
import nro.services.*;
import nro.services.*;

import java.util.HashMap;
import java.util.Map;
import nro.consts.ConstItem;
import nro.models.item.ItemOption;
import nro.models.player.Inventory;
import nro.server.ServerLog;
import nro.server.TaiXiu;
import nro.utils.Util;

/**
 * @author 💖 NgocCoder 💖
 * @copyright 💖 GirlkuN 💖
 */
public class Input {

    private static final Map<Integer, Object> PLAYER_ID_OBJECT = new HashMap<Integer, Object>();

    public static final int CHANGE_PASSWORD = 500;
    public static final int GIFT_CODE = 501;
    public static final int FIND_PLAYER = 502;
    public static final int CHANGE_NAME = 503;
    public static final int CHOOSE_LEVEL_BDKB = 5066;
    public static final int CHOOSE_LEVEL_KGHD = 5088;
    public static final int CHOOSE_LEVEL_CDRD = 7700;
    public static final int TANG_NGOC_HONG = 505;
    public static final int ADD_ITEM = 506;
public static final int TAI_taixiu = 323508;
    public static final int XIU_taixiu = 323505;
    public static final int CON_SO_MAY_MAN_NGOC = 507;

    public static final int CON_SO_MAY_MAN_VANG = 508;

    public static final int GIAI_TAN_BANG = 509;

    public static final int NHAN_THOI_VANG = 510;

    public static final int BAN_THOI_VANG = 511;
    
    public static final int SEND_ITEM_OP = 507;

    public static final byte NUMERIC = 0;
    public static final byte ANY = 1;
    public static final byte PASSWORD = 2;

    private static Input intance;

    private Input() {

    }

    public static Input gI() {
        if (intance == null) {
            intance = new Input();
        }
        return intance;
    }

    public void doInput(Player player, Message msg) {
        try {
            Player pl = null;
            String[] text = new String[msg.reader().readByte()];
            for (int i = 0; i < text.length; i++) {
                text[i] = msg.reader().readUTF();
            }
            switch (player.iDMark.getTypeInput()) {
                case CHANGE_PASSWORD:
                    Service.getInstance().changePassword(player, text[0], text[1], text[2]);
                    break;
                case GIFT_CODE:
                    GiftService.gI().use(player, text[0]);
                    break;
                case FIND_PLAYER:
                    pl = Client.gI().getPlayer(text[0]);
                    if (pl != null) {
                        NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_FIND_PLAYER, -1, "Ngài muốn..?",
                                new String[]{"Đi tới\n" + pl.name, "Gọi " + pl.name + "\ntới đây", "Đổi tên", "Ban"},
                                pl);
                    } else {
                        Service.getInstance().sendThongBao(player, "Người chơi không tồn tại hoặc đang offline");
                    }
                    break;
                case NHAN_THOI_VANG:
                    int soVang = Integer.parseInt(text[0]);
                    if (soVang <= 0) {
                        Service.getInstance().sendThongBao(player, "Số thỏi vàng không hợp lệ");
                        return;
                    }
                    Item thoivang = ItemService.gI().createNewConsignmentItem((short) ConstItem.THOI_VANG, soVang);
                    if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                        if (player.soThoiVang >= soVang) {
                            thoivang.itemOptions.add(new ItemOption(86, 0));
                            thoivang.itemOptions.add(new ItemOption(100, 1));
                            InventoryService.gI().addItemBag(player, thoivang, 99999);
                            PlayerDAO.subGoldBar(player, soVang);
                            player.soThoiVang -= soVang;
                            InventoryService.gI().sendItemBags(player);
                            Service.getInstance().sendThongBao(player, "Bạn nhận được " + thoivang.getName());
                        } else {
                            Service.getInstance().sendThongBao(player, "Bạn không đủ số thỏi vàng");
                        }
                    } else {
                        Service.getInstance().sendThongBao(player, "Cần 1 ô trống trong hành trang");
                    }
                    break;
                case CHANGE_NAME:
                    Player plChanged = (Player) PLAYER_ID_OBJECT.get((int) player.id);
                    if (plChanged != null) {
                        if (PlayerDAO.isExistName(text[0])) {
                            Service.getInstance().sendThongBao(player, "Tên nhân vật đã tồn tại");
                        } else {
                            plChanged.name = text[0];
                            PlayerDAO.saveName(plChanged);
                            Service.getInstance().player(plChanged);
                            Service.getInstance().Send_Caitrang(plChanged);
                            Service.getInstance().sendFlagBag(plChanged);
                            Zone zone = plChanged.zone;
                            ChangeMapService.gI().changeMap(plChanged, zone, plChanged.location.x, plChanged.location.y);
                            Service.getInstance().sendThongBao(plChanged, "Chúc mừng bạn đã có cái tên mới đẹp đẽ hơn tên ban đầu");
                            Service.getInstance().sendThongBao(player, "Đổi tên người chơi thành công");
                        }
                    }
                    break;
                case SEND_ITEM_OP:
                    if (player.isAdmin()) {
                        int idItemBuff = Integer.parseInt(text[1]);
                        int idOptionBuff = Integer.parseInt(text[2]);
                        int slOptionBuff = Integer.parseInt(text[3]);
                        int slItemBuff = Integer.parseInt(text[4]);
                        Player pBuffItem = Client.gI().getPlayer(text[0]);
                        if (pBuffItem != null) {
                            String txtBuff = "Buff to player: " + pBuffItem.name + "\b";

                            switch (idItemBuff) {
                                case -1:
                                    pBuffItem.inventory.gold = Math.min(pBuffItem.inventory.gold + (long) slItemBuff, Inventory.LIMIT_GOLD);
                                    txtBuff += slItemBuff + " vàng\b";
                                    Service.getInstance().sendMoney(player);
                                    ServerLog.logAdmin(pBuffItem.name, slItemBuff);
                                    break;
                                case -2:
                                    pBuffItem.inventory.gem = Math.min(pBuffItem.inventory.gem + slItemBuff, 2000000000);
                                    txtBuff += slItemBuff + " ngọc\b";
                                    Service.getInstance().sendMoney(player);
                                    ServerLog.logAdmin(pBuffItem.name, slItemBuff);
                                    break;
                                case -3:
                                    pBuffItem.inventory.ruby = Math.min(pBuffItem.inventory.ruby + slItemBuff, 2000000000);
                                    txtBuff += slItemBuff + " ngọc khóa\b";
                                    Service.getInstance().sendMoney(player);
                                    ServerLog.logAdmin(pBuffItem.name, slItemBuff);
                                    break;
                                default:
                                    Item itemBuffTemplate = ItemService.gI().createNewItem((short) idItemBuff);
                                    itemBuffTemplate.itemOptions.add(new ItemOption(idOptionBuff, slOptionBuff));
                                    itemBuffTemplate.quantity = slItemBuff;
                                    txtBuff += "x" + slItemBuff + " " + itemBuffTemplate.template.name + "\b";
                                    InventoryService.gI().addItemBag(pBuffItem, itemBuffTemplate, slItemBuff);
                                    ServerLog.logAdmin(pBuffItem.name, slItemBuff);
                                    InventoryService.gI().sendItemBags(pBuffItem);
                                    break;
                            }
                            NpcService.gI().createTutorial(player, 24, txtBuff);
                            if (player.id != pBuffItem.id) {
                                NpcService.gI().createTutorial(player, 24, txtBuff);
                            }
                        } else {
                            Service.getInstance().sendThongBao(player, "Player không online");
                        }
                        break;
                    }
                    break;
            
                case CHOOSE_LEVEL_BDKB: {
                    int level = Integer.parseInt(text[0]);
                    if (level >= 1 && level <= 110) {
                        Npc npc = NpcManager.getByIdAndMap(ConstNpc.QUY_LAO_KAME, player.zone.map.mapId);
                        if (npc != null) {
                            npc.createOtherMenu(player, ConstNpc.MENU_ACCEPT_GO_TO_BDKB,
                                    "Con có chắc muốn đến\nhang kho báu cấp độ " + level + "?",
                                    new String[]{"Đồng ý", "Từ chối"}, level);
                        }
                    } else {
                        Service.getInstance().sendThongBao(player, "Không thể thực hiện");
                    }
                }
                break;
                case CHOOSE_LEVEL_KGHD: {
                    int level = Integer.parseInt(text[0]);
                    if (level >= 1 && level <= 110) {
                        Npc npc = NpcManager.getByIdAndMap(ConstNpc.MR_POPO, player.zone.map.mapId);
                        if (npc != null) {
                            npc.createOtherMenu(player, ConstNpc.MENU_ACCEPT_GO_TO_KGHD,
                                    "Cậu có chắc muốn đến\nDestron Gas cấp độ " + level + "?",
                                    new String[]{"Đồng ý", "Từ chối"}, level);
                        }
                    } else {
                        Service.getInstance().sendThongBao(player, "Không thể thực hiện");
                    }
                }
                break;
                case CHOOSE_LEVEL_CDRD: {
                    int level = Integer.parseInt(text[0]);
                    if (level >= 1 && level <= 110) {
                        Npc npc = NpcManager.getByIdAndMap(ConstNpc.THAN_VU_TRU, player.zone.map.mapId);
                        if (npc != null) {
                            npc.createOtherMenu(player, ConstNpc.MENU_ACCEPT_GO_TO_CDRD,
                                    "Con có chắc chắn muốn đến con đường rắn độc cấp độ " + level + "?",
                                    new String[]{"Đồng ý", "Từ chối"}, level);
                        }
                    } else {
                        Service.getInstance().sendThongBao(player, "Không thể thực hiện");
                    }
                }
                break;
                              case TAI_taixiu:
                    int sotvxiu1 = Integer.valueOf(text[0]);
                    try {
                        if (sotvxiu1 >= 1000 && sotvxiu1 <= 1000000) {
                            if (player.inventory.ruby >= sotvxiu1) {
                                player.inventory.ruby -= sotvxiu1;
                                player.goldTai += sotvxiu1;
                                player.taixiu.toptaixiu += sotvxiu1;
                                TaiXiu.gI().goldTai += sotvxiu1;
                                Service.getInstance().sendThongBao(player, "Bạn đã đặt " + Util.format(sotvxiu1) + " Hồng ngọc vào TÀI");
                                TaiXiu.gI().addPlayerTai(player);
                                InventoryService.gI().sendItemBags(player);
                                Service.getInstance().sendMoney(player);
                            } else {
                                Service.getInstance().sendThongBao(player, "Bạn không đủ Hồng ngọc để chơi.");
                            }
                        } else {
                            Service.getInstance().sendThongBao(player, "Cược ít nhất 10.000 Hồng ngọc.");
                        }
                    } catch (NumberFormatException e) {
                        Service.getInstance().sendThongBao(player, "Số tiền cược không hợp lệ.");
                    } catch (Exception e) {
                        e.printStackTrace();
                        Service.getInstance().sendThongBao(player, "Đã xảy ra lỗi khi xử lý cược.");
                        System.out.println("Lỗi khi xử lý cược: " + e.getMessage());
                    }
                    break;
                case XIU_taixiu:
                    int sotvxiu2 = Integer.valueOf(text[0]);
                    try {
                        if (sotvxiu2 >= 1000 && sotvxiu2 <= 1000000) {
                            if (player.inventory.ruby >= sotvxiu2) {
                                player.inventory.ruby -= sotvxiu2;
                                player.goldXiu += sotvxiu2;
                                player.taixiu.toptaixiu += sotvxiu2;
                                TaiXiu.gI().goldXiu += sotvxiu2;
                                Service.getInstance().sendThongBao(player, "Bạn đã đặt " + Util.format(sotvxiu2) + " Hồng ngọc vào XỈU");
                                TaiXiu.gI().addPlayerXiu(player);
                                InventoryService.gI().sendItemBags(player);
                                Service.getInstance().sendMoney(player);

                            } else {
                                Service.getInstance().sendThongBao(player, "Bạn không đủ Hồng ngọc để chơi.");
                            }
                        } else {
                            Service.getInstance().sendThongBao(player, "Cược ít nhất 20.000 - 1.000.000 Hồng ngọc ");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Service.getInstance().sendThongBao(player, "Lỗi.");
                        System.out.println("nnnnn2  ");
                    }
                    break;
                case BAN_THOI_VANG:
                    long soLuong = Long.parseLong(text[0]);
                    Item thoiVang = InventoryService.gI().findItemBagByTemp(player, (short) 457);
                    if(soLuong < 0){
                        Service.getInstance().sendThongBao(player, "Đã bán " + soLuong + " bãi cứt" + " thu được 1" + " vàng");
                        return;
                    }
                    if (soLuong <= thoiVang.quantity) {
                        long goldNhanDuoc = soLuong * 500000000;
                        long soGoldAll = goldNhanDuoc + player.inventory.gold;
                        if (soGoldAll <= player.inventory.getGoldLimit()) {
                            player.inventory.gold += (soLuong * 500000000);
                            InventoryService.gI().subQuantityItemsBag(player, thoiVang, (int) soLuong);
                            InventoryService.gI().sendItemBags(player);
                            Service.getInstance().sendMoney(player);
                            Service.getInstance().sendThongBao(player, "Đã bán " + soLuong + " " + thoiVang.getName() + " thu được " + Util.numberToMoney(goldNhanDuoc) + " vàng");
                        } else {
                            Service.getInstance().sendThongBao(player, "Số vàng sau khi bán vượt quá số vàng có thể lưu trữ");
                        }
                    } else {
                        Service.getInstance().sendThongBao(player, "Không đủ thỏi vàng để bán");
                    }
                    break;
                case GIAI_TAN_BANG:
                    if (text[0] != null) {
                        String OK = text[0].toString();
                        if ("OK".equalsIgnoreCase(OK)) {
                            ClanService.gI().RemoveClanAll(player);
                        }
                    }
                    break;
                case TANG_NGOC_HONG:
                    pl = Client.gI().getPlayer(text[0]);
                    int numruby = Integer.parseInt((text[1]));
                    if (pl != null) {
                        if (numruby > 0 && player.inventory.ruby >= numruby) {
                            Item item = InventoryService.gI().findVeTangNgoc(player);
                            player.inventory.subRuby(numruby);
                            PlayerService.gI().sendInfoHpMpMoney(player);
                            pl.inventory.ruby += numruby;
                            PlayerService.gI().sendInfoHpMpMoney(pl);
                            Service.getInstance().sendThongBao(player, "Tặng Hồng ngọc thành công");
                            Service.getInstance().sendThongBao(pl, "Bạn được " + player.name + " tặng " + numruby + " Hồng ngọc");
                            InventoryService.gI().subQuantityItemsBag(player, item, 1);
                            InventoryService.gI().sendItemBags(player);
                        } else {
                            Service.getInstance().sendThongBao(player, "Không đủ Hồng ngọc để tặng");
                        }
                    } else {
                        Service.getInstance().sendThongBao(player, "Người chơi không tồn tại hoặc đang offline");
                    }
                    break;
                
                case CON_SO_MAY_MAN_VANG:
                    int CSMM2 = Integer.parseInt(text[0]);
                    if (CSMM2 >= MiniGame.gI().MiniGame_S1.min && CSMM2 <= MiniGame.gI().MiniGame_S1.max && MiniGame.gI().MiniGame_S1.second > 10) {
                        MiniGame.gI().MiniGame_S1.newData(player, CSMM2, 1);
                    }
                    break;
                case ADD_ITEM:
                    short id = Short.parseShort((text[0]));
                    int quantity = Integer.parseInt(text[1]);
                    Item item = ItemService.gI().createNewItem(id);
                    if (item.template.type < 7) {
                        for (int i = 0; i < quantity; i++) {
                            item = ItemService.gI().createNewItem(id);
                            RewardService.gI().initBaseOptionClothes(item.template.id, item.template.type, item.itemOptions);
                            InventoryService.gI().addItemBag(player, item, 0);
                        }
                    } else {
                        item.quantity = quantity;
                        InventoryService.gI().addItemBag(player, item, 0);
                    }
                    InventoryService.gI().sendItemBags(player);
                    Service.getInstance().sendThongBao(player, "Bạn nhận được " + item.template.name + " Số lượng: " + quantity);
            }
        } catch (Exception e) {
        }
    }

    public void createForm(Player pl, int typeInput, String title, SubInput... subInputs) {
        pl.iDMark.setTypeInput(typeInput);
        Message msg;
        try {
            msg = new Message(-125);
            msg.writer().writeUTF(title);
            msg.writer().writeByte(subInputs.length);
            for (SubInput si : subInputs) {
                msg.writer().writeUTF(si.name);
                msg.writer().writeByte(si.typeInput);
            }
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void createFormChangePassword(Player pl) {
        createForm(pl, CHANGE_PASSWORD, "Đổi Mật Khẩu", new SubInput("Nhập mật khẩu cũ", PASSWORD),
                new SubInput("Mật khẩu mới", PASSWORD),
                new SubInput("Nhập lại mật khẩu mới", PASSWORD));
    }

    public void createFormGiftCode(Player pl) {
        if (pl.zone.map.mapId == 5 || pl.zone.map.mapId == 20 || pl.zone.map.mapId == 13) {
            createForm(pl, GIFT_CODE, "Mã quà tặng gồm 12 ký tự", new SubInput("Gift Code", ANY));
        } else {
            createForm(pl, GIFT_CODE, "Mã quà tặng", new SubInput("Nhập mã quà tặng", ANY));
        }
    }

    public void createFormFindPlayer(Player pl) {
        createForm(pl, FIND_PLAYER, "Tìm kiếm người chơi", new SubInput("Tên người chơi", ANY));
    }
    public void createFormSenditem1(Player pl) {
        createForm(pl, SEND_ITEM_OP, "SEND Vật Phẩm Option",
                new SubInput("Tên người chơi", ANY),
                new SubInput("ID Trang Bị", NUMERIC),
                new SubInput("ID Option", NUMERIC),
                new SubInput("Param", NUMERIC),
                new SubInput("Số lượng", NUMERIC));
    }

    public void createFormNhanThoiVang(Player pl) {
        createForm(pl, NHAN_THOI_VANG, "Nhập thỏi vàng", new SubInput("Nhập thỏi vàng", NUMERIC));
    }

        public void TAI_taixiu(Player pl) {
        createForm(pl, TAI_taixiu, "Chọn số hồng ngọc đặt Tài", new SubInput("Số Hồng ngọc cược", ANY));//????
    }

    public void XIU_taixiu(Player pl) {
        createForm(pl, XIU_taixiu, "Chọn số hồng ngọc đặt Xỉu", new SubInput("Số Hồng ngọc cược", ANY));//????
    }

    
    public void createFormChangeName(Player pl, Player plChanged) {
        PLAYER_ID_OBJECT.put((int) pl.id, plChanged);
        createForm(pl, CHANGE_NAME, "Đổi tên " + plChanged.name, new SubInput("Tên mới", ANY));
    }

    public void createFormChooseLevelBDKB(Player pl) {
        createForm(pl, CHOOSE_LEVEL_BDKB, "Hãy chọn cấp độ hang kho báu từ 1-110", new SubInput("Cấp độ", NUMERIC));
    }

    public void createFormChooseLevelKhiGas(Player pl) {
        createForm(pl, CHOOSE_LEVEL_KGHD, "Hãy chọn cấp độ từ 1-110", new SubInput("Cấp độ", NUMERIC));
    }

    public void createFormChooseLevelCDRD(Player pl) {
        createForm(pl, CHOOSE_LEVEL_CDRD, "Chọn cấp độ", new SubInput("Cấp độ (1-110)", NUMERIC));
    }

    public void createFormGiaiTanBang(Player pl) {
        createForm(pl, GIAI_TAN_BANG, "Nhập OK để xác nhận giải tán bang hội.", new SubInput("", ANY));
    }

    public void createFormTangRuby(Player pl) {
        createForm(pl, TANG_NGOC_HONG, "Tặng ngọc", new SubInput("Tên nhân vật", ANY),
                new SubInput("Số Hồng Ngọc Muốn Tặng", NUMERIC));
    }

    public void createFormBanThoiVang(Player pl) {
        createForm(pl, BAN_THOI_VANG, "Bạn muốn bán bao nhiêu [Thỏi vàng] ?", new SubInput("Số lượng", NUMERIC));
    }

    public void createFormAddItem(Player pl) {
        createForm(pl, ADD_ITEM, "Add Item", new SubInput("ID VẬT PHẨM", NUMERIC),
                new SubInput("SỐ LƯỢNG", NUMERIC));
    }

    public void createFormConSoMayMan_Gem(Player pl) {
        createForm(pl, CON_SO_MAY_MAN_NGOC, "Hãy chọn 1 số từ 0 đến 99 giá 5 ngọc", new SubInput("Số bạn chọn", NUMERIC));
    }

    public void createFormConSoMayMan_Gold(Player pl) {
        createForm(pl, CON_SO_MAY_MAN_VANG, "Hãy chọn 1 số từ 0 đến 99 giá 1.000.000 vàng", new SubInput("Số bạn chọn", NUMERIC));
    }

    public class SubInput {

        private String name;
        private byte typeInput;

        public SubInput(String name, byte typeInput) {
            this.name = name;
            this.typeInput = typeInput;
        }
    }

}
