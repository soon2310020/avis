package com.stg.service.caching.base.gson;

import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public abstract class CachingService2 implements CommandsV2 {
    protected final RedisCommands<String, String> redis;


    /***/
    @Override
    public String get(String key) {
        return redis.get(key);
    }

    @Override
    public boolean set(String key, String value) {
        redis.set(key, value);
        // EXEC_RESPONSE.equals(redis.set(key, value));
        return true;
    }

    @Override
    public boolean set(String key, String value, long ttlSecond) {
        redis.set(key, value, SetArgs.Builder.ex(ttlSecond));
        // EXEC_RESPONSE.equals(redis.set(key, value, SetArgs.Builder.ex(ttlSecond)));
        return true;
    }

    @Override
    public boolean delKeyPattern(String keyPattern) {
        log.info("Starting... delKeyPattern({})", keyPattern);
        redis.eval("local keys = redis.call('keys', ARGV[1]) for i=1,#keys,5000 do redis.call('del', unpack(keys, i, math.min(i+4999, #keys))) end return keys",
                ScriptOutputType.VALUE,
                new String[]{},
                keyPattern
        );
        log.info("End delKeyPattern({})", keyPattern);
        return true;
    }

    private static final String EXISTS_PATTERN = "local re = ARGV[1]\n" +
            "local nt = ARGV[2]\n" +
            "\n" +
            "local cur = 0\n" +
            "local rep = {}\n" +
            "local tmp\n" +
            "\n" +
            /*"if not re then\n" +
            "  re = \".*\"\n" +
            "end\n" +
            "\n" +*/
            "repeat\n" +
            "  tmp = redis.call(\"SCAN\", cur, \"MATCH\", \"*\")\n" +
            "  cur = tonumber(tmp[1])\n" +
            "  if tmp[2] then\n" +
            "    for k, v in pairs(tmp[2]) do\n" +
            "      local fi = v:find(re) \n" +
            "      if (fi and not nt) or (not fi and nt) then\n" +
            "        rep[#rep+1] = v\n" +
            "      end\n" +
            "    end\n" +
            "  end\n" +
            "until cur == 0\n" +
            "return rep";
    @Override
    public boolean existByPattern(String keyPattern) {
        log.debug("Starting... existByPattern({})", keyPattern);

        return null != redis.eval(EXISTS_PATTERN,
                ScriptOutputType.VALUE,
                new String[]{},
                keyPattern
        );
    }

    /***/
    @Override
    public boolean mset(@NonNull Map<String, String> valueMap) {
        redis.mset(valueMap);
        return true;
    }
}
