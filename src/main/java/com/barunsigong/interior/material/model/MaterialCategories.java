package com.barunsigong.interior.material.model;

import java.util.List;

/** Independent first- and second-depth code catalogs. */
public final class MaterialCategories {

    public record Item(String code, String label) {}

    private static Item i(String code, String label) {
        return new Item(code, label);
    }

    public static final List<Item> ALL = List.of(
        i("ENTRANCE", "현관"),
        i("ENTRANCE_DOOR", "현관중문"),
        i("WALLPAPER", "도배"),
        i("FLOORING", "바닥재"),
        i("CARPENTRY", "목공"),
        i("KITCHEN", "주방"),
        i("FURNITURE", "가구"),
        i("LIVING_BATHROOM", "거실화장실"),
        i("MASTER_BATHROOM", "안방화장실"),
        i("BALCONY", "베란다")
    );
    public static final List<Item> ITEMS = List.of(
        i("FIRE_DOOR_FILM", "방화문 필름"),
        i("FLOOR_TILE", "바닥타일"),
        i("DOOR", "문(중문 포함)"),
        i("FILM", "필름"),
        i("GLASS", "유리"),
        i("PARTITION", "파티션"),
        i("WALLPAPER", "도배"),
        i("FLOORING", "바닥재"),
        i("CEILING_MOLDING", "천정(마이너스몰딩)"),
        i("BASEBOARD", "바닥(걸레받이 몰딩)"),
        i("DOOR_FRAME", "문틀"),
        i("HANDLE", "손잡이"),
        i("FAUCET", "수전"),
        i("SINK", "싱크볼"),
        i("UPPER_CABINET", "상단장"),
        i("LOWER_CABINET", "하단장"),
        i("COUNTERTOP", "상판"),
        i("WALL_TILE", "벽체타일"),
        i("DOOR_PANEL", "문짝"),
        i("WARDROBE", "여닫이장"),
        i("SHOE_CABINET", "신발장"),
        i("BASIN_FAUCET", "세면수전"),
        i("BASIN", "세면대"),
        i("TOILET", "변기"),
        i("SHOWER_FAUCET", "샤워 수전"),
        i("SLIDE_BAR", "슬라이드바"),
        i("PAPER_HOLDER", "휴지걸이"),
        i("TOWEL_BAR", "수건걸이"),
        i("SLIDING_CABINET", "슬라이드장"),
        i("VENTILATOR", "환풍기"),
        i("CORNER_SHELF", "코너선반")
    );

    public static boolean valid(String category, String subcategory) {
        return (
            ALL.stream().anyMatch(i -> i.code().equals(category)) &&
            ITEMS.stream().anyMatch(i -> i.code().equals(subcategory))
        );
    }

    public static String categoryLabel(String code) {
        return ALL.stream()
            .filter(i -> i.code().equals(code))
            .map(Item::label)
            .findFirst()
            .orElse(code);
    }

    public static String itemLabel(String code) {
        return ITEMS.stream()
            .filter(i -> i.code().equals(code))
            .map(Item::label)
            .findFirst()
            .orElse(code == null ? "미분류" : code);
    }

    private MaterialCategories() {}
}
