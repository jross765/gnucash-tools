package org.gnucash.tools.xml.gen.complex;

public class ParamTuple
{
	// ---------------------------------------------------------------
	
	public String type;                // 0
	public String stockAcctID;         // 1
	public String incomeAcctID;        // 2
	public String expensesAcctAmtList; // 3
	public String offsetAcctID;        // 4
	public String nofStocks;           // 5
	public String stockPrc;            // 6
	public String divDistrGross;       // 7
	public String stockSplitFactor;    // 8
	public String dateFormat;          // 9
	public String datPst;              // 10
	public String descr;               // 11
	
	// ---------------------------------------------------------------
	
	public ParamTuple()
	{
		type = null;
		stockAcctID = null;
		incomeAcctID = null;
		expensesAcctAmtList = null;
		offsetAcctID = null;
		nofStocks = null;
		stockPrc = null;
		divDistrGross = null;
		stockSplitFactor = null;
		dateFormat = null;
		datPst = null;
		descr = null;
	}
	
	public ParamTuple(String type,
					  String stockAcctID,
					  String incomeAcctID,
					  String expensesAcctAmtList,
					  String offsetAcctID,
					  String nofStocks,
					  String stockPrc,
					  String divDistrGross,
					  String stockSplitFactor,
					  String dateFormat,
					  String datPst,
					  String descr)
	{
		this.type = type;
		this.stockAcctID = stockAcctID;
		this.incomeAcctID = incomeAcctID;
		this.expensesAcctAmtList = expensesAcctAmtList;
		this.offsetAcctID = offsetAcctID;
		this.nofStocks = nofStocks;
		this.stockPrc = stockPrc;
		this.divDistrGross = divDistrGross;
		this.stockSplitFactor = stockSplitFactor;
		this.dateFormat = dateFormat;
		this.datPst = datPst;
		this.descr = descr;
	}

	// ---------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return "ParamTuple [type='" + type + "', " +
		            "stockAcctID='" + stockAcctID + "', " +
		           "incomeAcctID='" + incomeAcctID +  "', " +
		    "expensesAcctAmtList='" + expensesAcctAmtList + "', " +
		           "offsetAcctID='" + offsetAcctID +  "', " +
		              "nofStocks='" + nofStocks +  "', " +
		               "stockPrc='" + stockPrc +  "', " +
		          "divDistrGross='" + divDistrGross +  "', " +
		       "stockSplitFactor='" + stockSplitFactor +  "', " +
		             "dateFormat='" + dateFormat +  "', " +
		                 "datPst='" + datPst +  "', " +
		                  "descr='" + descr + "']";
	}

}
