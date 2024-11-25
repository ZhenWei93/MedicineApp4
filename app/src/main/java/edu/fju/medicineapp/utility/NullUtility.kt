package edu.fju.medicineapp.utility

import android.view.View

/**
 * Description:
 *
 * Author: Shi_Kai_Lin
 *
 * Date: 2024/6/19
 */
class NullUtility
{
    companion object
    {
        private val TAG = NullUtility::class.java.simpleName.toString()

//==================================================================================================
// 判斷 物件，Collection，Map，Array
//==================================================================================================
        fun isNullOrEmpty(obj: Any?): Boolean
        {
            tryCatch()
            {
                // 檢查是否為 null
                if (obj == null) return true

                // 檢查是否為 Collection 型態，並確認其是否為空
                if (obj is Collection<*>) return obj.isEmpty()

                // 檢查是否為 Map 型態，並確認其是否沒有元素
                if (obj is Map<*, *>) return obj.isEmpty()

                // 檢查是否為 Array 型態，並確認其是否為空
                if (obj is Array<*>) return obj.isEmpty()
            }

            // 其他情況下返回 false
            return false
        }

//==================================================================================================
// try cache
//==================================================================================================
        inline fun <T> tryCatch(block: () -> T): T? = try
        {
            block()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            null
        }

//==================================================================================================
// let
//==================================================================================================
        inline fun <A, B, R>            let2(a: A?, b: B?,                      block: (A, B)           -> R): R? = a?.let{ b?.let{                         return block(a, b)              }}
        inline fun <A, B, C, R>         let3(a: A?, b: B?, c:C?,                block: (A, B, C)        -> R): R? = a?.let{ b?.let{ c?.let{                 return block(a, b, c)           }}}
        inline fun <A, B, C, D, R>      let4(a: A?, b: B?, c:C?, d:D?,          block: (A, B, C, D)     -> R): R? = a?.let{ b?.let{ c?.let{ d?.let{         return block(a, b, c, d)        }}}}
        inline fun <A, B, C, D, E, R>   let5(a: A?, b: B?, c:C?, d:D?, e:E?,    block: (A, B, C, D, E)  -> R): R? = a?.let{ b?.let{ c?.let{ d?.let{ e?.let{ return block(a, b, c, d, e)     }}}}}

        inline fun <A, B, R>            let2Safe(a: A?, b: B?,                      block: (A, B)           -> R): R? = try{ let2(a,b,block) }          catch(e: Exception){ e.printStackTrace(); null }
        inline fun <A, B, C, R>         let3Safe(a: A?, b: B?, c: C?,               block: (A, B, C)        -> R): R? = try{ let3(a,b,c,block) }        catch(e: Exception){ e.printStackTrace(); null }
        inline fun <A, B, C, D, R>      let4Safe(a: A?, b: B?, c: C?, d: D?,        block: (A, B, C, D)     -> R): R? = try{ let4(a,b,c,d,block) }      catch(e: Exception){ e.printStackTrace(); null }
        inline fun <A, B, C, D, E, R>   let5Safe(a: A?, b: B?, c: C?, d: D?, e:E?,  block: (A, B, C, D, E)  -> R): R? = try{ let5(a,b,c,d,e,block) }    catch(e: Exception){ e.printStackTrace(); null }

//==================================================================================================
// 方便 條件判斷時遇到 null 狀況
//==================================================================================================

        // 為空反為 false; 為真執行 action()
        // 與條件判斷一起用
        inline fun <T> nullIsFalse(me:T?, action: (T) -> Boolean): Boolean
        {
            if (me != null)
            {
                return action(me)
            }
            else
            {
                return false
            }
        }

        // 不為空執行 condition 並且 condition 為真才執行 action
        // 多是自己當條件判斷
        inline fun <T> nullIsFalse(me: T?, condition: (T) -> Boolean, action: (T) -> Unit)
        {
            if (me != null)
            {
                if (condition(me))
                    action(me)
            }
        }

//==================================================================================================
// vararg
//==================================================================================================
        fun  <T> isVarargNull(tag: String, vararg values: T?): Boolean
        {
            // 外面的人呼叫isVarargNull時，記得如果也是數組要傳入isVarargNull記得該傳入參數要先 * 解開，才不會發生數組中的數組
            // 另外澄清一下 要記得
            // 1.如果是用 isVarargNull(TAG)        這樣的情況 values 裡面真的空   但依舊不是表示  values = null  ==> []
            // 2.如果是用 isVarargNull(TAG, null)  這樣的情況 values 有一個元素為空                             ==> [null]
            if (values.isEmpty())
                return true

            SOUT.Loge(tag, "${values.size}, is Not Null")
            return false
// 以下判斷都沒有用
//            if (values == null) 這個檢查是不必要的，因為 values 是一個可變參數，它不會為 null。即使沒有傳遞任何參數，values 也會被初始化為一個空數組。
//            if (values[0] == null) 這個檢查可能會導致 IndexOutOfBoundsException 異常，因為您假設數組中至少有一個元素，但實際上可能沒有。
        }

        fun  <T> getVarargValue(index: Int, vararg values: T?): T?
        {
            // 外面的人要getVarargValue時，記得如果也是數組要傳入getVarargValue記得該傳入參數要先 * 解開，才不會發生數組中的數組
            if (values.isEmpty())
                return null

            if (index<0 && index>=values.size)
                return null

            return values[index]
        }

// List系列
        fun <K, V> getSize(map: Map<K, V>?) = map?.size ?: 0
        fun <T> getSize(list: List<T>?) = list?.size ?: 0

        fun <T> getItem(list: List<T>?, index: Int): T?
        {
            if (list == null)
                return null

            if (index in list.indices)
            {
                return list[index]
            }

            return null
        }

        fun <T> getItemFromViewTag(view: View, list: List<T>?, whenGetItem: (item: T)->(Unit))
        {
            ((view.tag as? Int)?.let {iPosition->getItem(list, iPosition)} as? T)?.let()
            { item ->
                whenGetItem(item)
            }
// 用法
//        getItemFromViewTag(it)
//        { item: RegistrationInfo ->
//        }
        }
    }
}