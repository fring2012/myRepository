package com.example.c.dao.code.code.code;

public interface Dao<V> {
    /**
     *
     * @return
     */
    V getBeen(int id);

    /**
     *
     * @param v
     * @return
     */
    int insertBeen(V v);

    /**
     *
     * @param v
     * @return
     */
    int updateBeenForId(V v);

    /**
     *
     * @param v
     * @return
     */
    int deleteBeen(V v);

}
