package net.lorgen.easydb;

public enum DataAccessFrequency {
    RARELY(DatabaseType.SQL),
    OCCASIONALLY(DatabaseType.MONGODB),
    FREQUENTLY(DatabaseType.REDIS);

    private DatabaseType recommendedType;

    DataAccessFrequency(DatabaseType recommendedType) {
        this.recommendedType = recommendedType;
    }

    public DatabaseType getRecommendedType() {
        return recommendedType;
    }
}
