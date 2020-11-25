package com.elytraforce.bungeesuite.rappu_b;

import java.sql.SQLException;

public interface Callback<T>
{
    void callback(final T p0) throws SQLException;
}

