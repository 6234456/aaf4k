package eu.qiou.aaf4k.reportings.etl

import eu.qiou.aaf4k.reportings.model.ProtoAccount
import eu.qiou.aaf4k.reportings.model.ProtoReporting
import eu.qiou.aaf4k.util.time.TimeParameters

/**
 * in some cases, the account data info is unknown, but the structure is pre-defined based on, for example, the account id.
 * As to the SKR3 of the German accounting, the accounts with an id ranging from 8400 to 8900 belongs to category the revenue.
 * When the definition of the new accounts follows the given pattern, i.e. regulation of the accounting frame, there exists no need to pre-define the whole structure in advance.
 *
 * the orginially loosely coupled data and structure are combined in the accounting frame
 */

abstract class AccountingFrame(id: Int, name: String, accounts: List<ProtoAccount>) : ProtoReporting(id, name, timeParameters = TimeParameters(), structure = accounts)