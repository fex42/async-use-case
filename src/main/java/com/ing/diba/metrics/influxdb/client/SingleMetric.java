package com.ing.diba.metrics.influxdb.client;




import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;




public class SingleMetric
{

    private static final double ROUND_FACTOR = 1000d;




	public static boolean isFinite(final double paramDouble)
    {
        return (Math.abs(paramDouble) <= 1.7976931348623157E+308D);
    }

    private Map< String, Object > fieldMap;

    private String                measurement;

    private Map< String, String > tagMap;

    private long                  timestamp = 0;

    private TimeUnit              timeUnit  = TimeUnit.MILLISECONDS;
    
    private boolean               isRoundedDouble = true;




    public SingleMetric()
    {
        this.tagMap = new HashMap< String, String >();
        this.fieldMap = new HashMap< String, Object >();
    }




	private void appendBigDecimal(final Object value, final StringBuilder result) {
		final double valueDbl = ((Number) value).doubleValue();
		if (SingleMetric.isFinite(valueDbl))
		{
			if (isRoundedDouble)
			{
		        result.append(Math.round(valueDbl * ROUND_FACTOR) / ROUND_FACTOR);
			}
			else
			{
		        result.append(((BigDecimal) value).toPlainString());
			}
		}
		else
		{
		    result.append("0.0");
		}
	}




	private void appendBoolean(final Object value, final StringBuilder result) {
		result.append(String.valueOf(value));
	}




    private void appendDouble(final Object value, final StringBuilder result) {
		final double valueDbl = ((Number) value).doubleValue();
		if (SingleMetric.isFinite(valueDbl))
		{
			if (isRoundedDouble)
			{
				final double roundedValueDbl = Math.round(valueDbl * ROUND_FACTOR) / ROUND_FACTOR;
				if (Math.abs(valueDbl) > 10_000_000)
				{
			        result.append(BigDecimal.valueOf(roundedValueDbl).toPlainString());
				}
				else
				{
					result.append(roundedValueDbl);
				}
			}
			else
			{
		        result.append(BigDecimal.valueOf(valueDbl).toPlainString());
			}
		}
		else
		{
		    result.append("0.0");
		}
	}




	private void appendInteger(final Object value, final StringBuilder result) {
		appendBoolean(value, result);
		result.append('i');
	}




	private void appendString(final Object value, final StringBuilder result) {
		result.append('"');
		String strValue = String.valueOf(value);
		strValue = strValue.replace('"', '\'');
		result.append(strValue);
		result.append('"');
	}




	public String build()
    {
        final String fieldString = buildFieldString();
        return (fieldString.isEmpty() ? null : this.measurement + buildTagString() + " " + fieldString + (this.timestamp > 0 ? " " + this.timestamp : ""));
    }




	private String buildFieldString()
    {
        final StringBuilder result = new StringBuilder();

        boolean isFirst = true;
        for (final String key : this.fieldMap.keySet())
        {
            final Object value = this.fieldMap.get(key);

            boolean isValidValue = true;
            if (value instanceof Number)
            {
                isValidValue = (Math.abs(((Number) value).doubleValue()) <= 1.7976931348623157E+308D);
            }

            if (isValidValue)
            {
                final StringBuilder valueStr = buildFieldValue(value);
                if (valueStr.length() > 0)
                {
                    if (isFirst)
                    {
                        isFirst = false;
                    }
                    else
                    {
                        result.append(',');
                    }
                    result.append(key);
                    result.append('=');
                    result.append(valueStr);
                }
            }
        }

        return result.toString();
    }




	private StringBuilder buildFieldValue(final Object value)
    {
        final StringBuilder result = new StringBuilder();

        if ((value instanceof Float) || (value instanceof Double))
        {
            appendDouble(value, result);
        }
        else if (value instanceof BigDecimal)
        {
            appendBigDecimal(value, result);
        }
        else if ((value instanceof Short) || (value instanceof Integer) || (value instanceof Long) || (value instanceof BigInteger))
        {
            appendInteger(value, result);
        }
        else if (value instanceof Boolean)
        {
            appendBoolean(value, result);
        }
        else
        {
            appendString(value, result);
        }

        return result;
    }




    private String buildTagString()
    {
        final StringBuilder result = new StringBuilder();

        for (final String key : this.tagMap.keySet())
        {
            result.append(',');
            result.append(key);
            result.append('=');
            result.append(this.tagMap.get(key)
                          .replace(' ', '_'));
        }

        return result.toString();
    }




    public void clear()
    {
        this.timestamp = 0;
        this.measurement = null;
        this.tagMap.clear();
        this.fieldMap.clear();
    }




    public void convertToTimeUnit(final TimeUnit newTimeUnit)
    {
        if (this.timestamp > 0)
        {
            this.timestamp = newTimeUnit.convert(this.timestamp, this.timeUnit);
        }
    }




    public Map< String, Object > getFieldMap()
    {
        return this.fieldMap;
    }




    public String getMeasurement()
    {
        return this.measurement;
    }




    public Map< String, String > getTagMap()
    {
        return this.tagMap;
    }




    public long getTimestamp()
    {
        return this.timestamp;
    }




    public TimeUnit getTimeUnit()
    {
        return this.timeUnit;
    }




    public boolean isRoundedDouble() {
		return isRoundedDouble;
	}




    public void setFieldMap(final Map< String, Object > fieldMap)
    {
        this.fieldMap = fieldMap;
    }




    public void setIsRoundedDouble(boolean isRoundedDouble) {
		this.isRoundedDouble = isRoundedDouble;
	}




    public void setMeasurement(final String measurement)
    {
        this.measurement = measurement;
    }




    public void setTagMap(final Map< String, String > tagMap)
    {
        this.tagMap = tagMap;
    }




    public void setTimestamp(final long timestamp)
    {
        this.timestamp = timestamp;
    }




    public void setTimestampToNow()
    {
        this.timestamp = System.currentTimeMillis();
        this.timeUnit = TimeUnit.MILLISECONDS;
    }




    public void setTimeUnit(final TimeUnit newTimeUnit)
    {
        switch (newTimeUnit)
        {
            case DAYS:
                throw new IllegalArgumentException("Days are not allowed");
            default:
                break;
        }
        this.timeUnit = newTimeUnit;
    }

}
