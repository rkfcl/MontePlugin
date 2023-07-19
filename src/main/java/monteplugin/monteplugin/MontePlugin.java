package monteplugin.monteplugin;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public final class MontePlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    private Map<Player, Boolean> shuffleStatusMap = new HashMap<>(); // 섞기 동작 여부를 저장하는 변수
    private Map<Player, ItemStack> selectStatusMap = new HashMap<>(); // 야바위 선택 동작 여부 및 배팅 금액를 저장하는 변수
    @EventHandler
    public void onBlockPlace(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return; // 우클릭한 블럭이 아닌 경우 무시합니다.
        }
        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock != null && clickedBlock.getType() == Material.SEA_LANTERN) {
            openMonteInventory(player);
        }
    }
    @EventHandler
    public void ShopMenuInventory(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        Player player = (Player) event.getWhoClicked();
        Boolean shuffleStatus = shuffleStatusMap.get(player);
        ItemStack selectStatus = selectStatusMap.get(player);
        ClickType clickType = event.getClick();

        if (clickedInventory == null) return;

        // 야바위 상점
        if (event.getView().getTitle().equalsIgnoreCase("야바위")) {
            Inventory monteInventory = event.getView().getTopInventory();
            ItemStack targetSlotItem = monteInventory.getItem(22);
            event.setCancelled(true); // 이벤트 취소하여 아이템을 메뉴로 옮기지 못하도록 함
            if (clickedInventory.getType() == InventoryType.PLAYER) {
                ItemStack clickedItem = event.getCurrentItem();
                String amountCheck = clickedItem.getItemMeta().getDisplayName();
                int amount = Integer.parseInt(amountCheck.replace("§6골드",""));
                // 클릭한 인벤토리가 플레이어 인벤토리인 경우
                if (clickedItem.getType().equals(Material.HEART_OF_THE_SEA) && clickedItem.getItemMeta().getDisplayName().contains("§6골드") && amount <= 10000) {
                    ItemStack updatedClickedItem = clickedItem.clone();
                    if (targetSlotItem == null) {
                        if (clickedItem.getAmount() > 1) {
                            updatedClickedItem.setAmount(1);
                            clickedItem.setAmount(clickedItem.getAmount() - 1);
                        } else {
                            clickedItem.setAmount(clickedItem.getAmount() - 1);
                        }

                        monteInventory.setItem(22, updatedClickedItem);
                    }
                }else if(amount>100000){
                    player.sendMessage("§4[ 야바위 ] §f최대 배팅 금액은 100000$ 입니다.");
                }
            }
            if (event.getSlot() == 31 && targetSlotItem != null && (shuffleStatus == null || !shuffleStatus)) {
                shuffleStatusMap.put(player, true); // 해당 플레이어의 섞기 동작 상태를 true로 설정
                selectStatusMap.put(player, targetSlotItem);
                monteInventory.setItem(22,null);
                String amountCheck = targetSlotItem.getItemMeta().getDisplayName();
                int amount = Integer.parseInt(amountCheck.replace("§6골드",""));
                final int shuffleDelay = 3; // 섞이는 간격 (틱 단위, 20 틱 = 1초)
                final int shuffleCount = 20; // 섞이는 횟수

                final ItemStack item10 = monteInventory.getItem(10);
                final ItemStack item12 = monteInventory.getItem(12);
                final ItemStack item14 = monteInventory.getItem(14);
                final ItemStack item16 = monteInventory.getItem(16);

                // 클릭 비활성화
                event.setCancelled(true);

                new BukkitRunnable() {
                    int count = 0;
                    @Override
                    public void run() {
                        shuffleInventorySlots(monteInventory);

                        count++;

                        // 각 섞이는 단계에 따라 적절한 소리 출력
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 1.0f, 1.5f);

                        if (count >= shuffleCount) {
                            shuffleStatusMap.put(player, false); // 해당 플레이어의 섞기 동작 상태를 false로 설정
                            cancel();
                        }
                    }
                }.runTaskTimer(this, 0, shuffleDelay);
            } else if (event.getSlot() == 31 && (shuffleStatus != null || shuffleStatus)) {
                player.sendMessage("§4[ 야바위 ] §f이미 게임이 시작되었습니다.");
            }
            if ((event.getSlot() == 10 || event.getSlot() == 12 || event.getSlot() == 14 || event.getSlot() == 16) && (shuffleStatus == null || !shuffleStatus) && selectStatus != null) {
                Random random = new Random();
                int randomNumber = random.nextInt(4); // 0부터 3까지의 난수 생성

                if (randomNumber == 1) {
                    player.getInventory().addItem(selectStatusMap.get(player));
                    player.getInventory().addItem(selectStatusMap.get(player));
                    selectStatusMap.put(player,null);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 1.0f, 1.0f);
                    player.sendMessage("§4[ 야바위 ] §f축하합니다! 배팅 금액의 두 배를 획득하였습니다!");
                    // 플레이어에게 메시지 보내기 등 원하는 작업 수행
                } else {
                    selectStatusMap.put(player,null);
                    player.sendMessage("§4[ 야바위 ] §f아쉽게도 배팅에 실패하셨습니다.");
                    // 플레이어에게 메시지 보내기 등 원하는 작업 수행
                }
            }

        }
    }
    // 슬롯을 랜덤하게 섞는 메서드
    private void shuffleInventorySlots(Inventory inventory) {
        List<ItemStack> items = Arrays.asList(inventory.getItem(10), inventory.getItem(12), inventory.getItem(14), inventory.getItem(16));
        Collections.shuffle(items);
        inventory.setItem(10, items.get(0));
        inventory.setItem(12, items.get(1));
        inventory.setItem(14, items.get(2));
        inventory.setItem(16, items.get(3));
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        Player player = (Player) event.getPlayer();

        if (inventory.getType() == InventoryType.CHEST && event.getView().getTitle().equalsIgnoreCase("야바위")) {
            ItemStack itemInSlot = inventory.getItem(22);

            if (itemInSlot != null) {
                if (isInventoryFull(player.getInventory())) {
                    player.getWorld().dropItem(player.getLocation(), itemInSlot);
                } else {
                    player.getInventory().addItem(itemInSlot);
                }
                inventory.setItem(22, null);
            }

        }
    }
    public void openMonteInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 36, "야바위");
        CustomStack monte_cup_blue = CustomStack.getInstance("monte_cup_blue");
        CustomStack monte_cup_green = CustomStack.getInstance("monte_cup_green");
        CustomStack monte_cup_pink = CustomStack.getInstance("monte_cup_pink");
        CustomStack monte_cup_yellow = CustomStack.getInstance("monte_cup_yellow");
        ItemStack monte_cup_blueItemStack = monte_cup_blue.getItemStack();
        ItemStack monte_cup_greenItemStack = monte_cup_green.getItemStack();
        ItemStack monte_cup_pinkItemStack = monte_cup_pink.getItemStack();
        ItemStack monte_cup_yellowItemStack = monte_cup_yellow.getItemStack();
        for (int i=0;i<36;i++){
            if (i==10){
                setItem(inventory, i, monte_cup_blueItemStack);
            }else if(i==12){
                setItem(inventory, i, monte_cup_greenItemStack);
            }else if(i==14){
                setItem(inventory, i, monte_cup_pinkItemStack);
            }else if(i==16){
                setItem(inventory, i, monte_cup_yellowItemStack);
            }else if(i==22){

            } else if (i==31) {
                setItem(inventory, i, InvenDecoLIME_STAINED_GLASS_PANE());
            } else{
                setItem(inventory, i, InvenDecoLIGHT_BLUE_STAINED_GLASS_PANE());
            }
        }


        player.openInventory(inventory);


    }
    private void setItem(Inventory inventory, int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }

    public static ItemStack InvenDecoLIME_STAINED_GLASS_PANE() {
        ItemStack check = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
        ItemMeta meta = check.getItemMeta();
        meta.setDisplayName("§l§a[ 시작 ]");
        meta.setLore(Arrays.asList("§f클릭 시 게임를 시작합니다"));
        check.setItemMeta(meta);
        return check;
    }
    public static ItemStack InvenDecoLIGHT_BLUE_STAINED_GLASS_PANE() {
        ItemStack check = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1);
        ItemMeta meta = check.getItemMeta();
        meta.setDisplayName(" ");
        check.setItemMeta(meta);
        return check;
    }
    private boolean isInventoryFull(Inventory inventory) {
        for (ItemStack item : inventory.getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) {
                return false;
            }
        }
        return true;
    }
}
