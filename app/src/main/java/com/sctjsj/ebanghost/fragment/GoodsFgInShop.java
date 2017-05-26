package com.sctjsj.ebanghost.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.sctjsj.ebanghost.R;
import com.sctjsj.ebanghost.activity.MainShopActivity;
import com.sctjsj.ebanghost.adapter.GoodsClassifyRVAdapter;
import com.sctjsj.ebanghost.adapter.GoodsListRVAdapter;
import com.sctjsj.ebanghost.bean.Dish;
import com.sctjsj.ebanghost.bean.DishMenu;
import com.sctjsj.ebanghost.bean.ShopCart;
import com.sctjsj.ebanghost.interf.ShopCartImp;
import com.sctjsj.ebanghost.url.BnUrl;
import com.sctjsj.ebanghost.util.NumformatUtil;
import com.sctjsj.ebanghost.util.ProgressUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;

public class GoodsFgInShop extends Fragment implements GoodsClassifyRVAdapter.onItemSelectedListener,ShopCartImp {
    private final static String TAG = "GoodsFgInShop";
    private RecyclerView leftMenu;//左侧菜单栏
    private RecyclerView rightMenu;//右侧菜单栏
    private TextView headerView;
    private LinearLayout headerLayout;//右侧菜单栏最上面的菜单
    private DishMenu headMenu;
    private GoodsClassifyRVAdapter leftAdapter;
    private GoodsListRVAdapter rightAdapter;
    private ArrayList<DishMenu> dishMenuList;//数据源
    private boolean leftClickType = false;//左侧菜单点击引发的右侧联动
    private ShopCart shopCart;
    private ImageView shoppingCartView;
    private TextView totalPriceTextView;
    private TextView totalPriceNumTextView;
    private LinearLayout mainLayout;
    private ProgressUtil pUtil;
    //接收店铺id
    private int shopId;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fg_goods_in_shop,container,false);
        //获取店铺id
        shopId=((MainShopActivity)getActivity()).getShopId();
        dishMenuList= (ArrayList<DishMenu>) ((MainShopActivity)getActivity()).getDishMenuList();
        shopCart=new ShopCart();

        //绑定视图
        initView(view);
        initAdapter();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initView(View view){
        mainLayout = (LinearLayout) ((MainShopActivity)getActivity()).getMainLayout();
        leftMenu = (RecyclerView)view.findViewById(R.id.fg_goods_in_shop_rv_classify);
        rightMenu = (RecyclerView)view.findViewById(R.id.fg_goods_in_shop_rv_goods_list);
        headerView = (TextView)view.findViewById(R.id.right_menu_tv);
        headerLayout = (LinearLayout)view.findViewById(R.id.right_menu_item);
        shoppingCartView = (ImageView) ((MainShopActivity)getActivity()).getShoppingCartView();
        totalPriceTextView = (TextView) ((MainShopActivity)getActivity()).getTotalPriceTextView();
        totalPriceNumTextView = (TextView) ((MainShopActivity)getActivity()).getTotalPriceNumTextView();
        leftMenu.setLayoutManager(new LinearLayoutManager(getActivity()));
        rightMenu.setLayoutManager(new LinearLayoutManager(getActivity()));
        rightMenu.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if( recyclerView.canScrollVertically(1)==false) {//无法下滑
                    showHeadView();
                    return;
                }
                View underView = null;
                if(dy>0)
                    underView = rightMenu.findChildViewUnder(headerLayout.getX(),headerLayout.getMeasuredHeight()+1);
                else
                    underView = rightMenu.findChildViewUnder(headerLayout.getX(),0);
                if(underView!=null && underView.getContentDescription()!=null ){
                    int position = Integer.parseInt(underView.getContentDescription().toString());
                    DishMenu menu = rightAdapter.getMenuOfMenuByPosition(position);

                    if(leftClickType || !menu.getMenuName().equals(headMenu.getMenuName())) {
                        if (dy> 0 && headerLayout.getTranslationY()<=1 && headerLayout.getTranslationY()>= -1 * headerLayout.getMeasuredHeight()*4/5 && !leftClickType) {// underView.getTop()>9
                            int dealtY = underView.getTop() - headerLayout.getMeasuredHeight();
                            headerLayout.setTranslationY(dealtY);
//                            Log.e(TAG, "onScrolled: "+headerLayout.getTranslationY()+"   "+headerLayout.getBottom()+"  -  "+headerLayout.getMeasuredHeight() );
                        }
                        else if(dy<0 && headerLayout.getTranslationY()<=0 && !leftClickType) {
                            headerView.setText(menu.getMenuName());
                            int dealtY = underView.getBottom() - headerLayout.getMeasuredHeight();
                            headerLayout.setTranslationY(dealtY);
//                            Log.e(TAG, "onScrolled: "+headerLayout.getTranslationY()+"   "+headerLayout.getBottom()+"  -  "+headerLayout.getMeasuredHeight() );
                        }
                        else{
                            headerLayout.setTranslationY(0);
                            headMenu = menu;
                            headerView.setText(headMenu.getMenuName());
                            for (int i = 0; i < dishMenuList.size(); i++) {
                                if (dishMenuList.get(i) == headMenu) {
                                    leftAdapter.setSelectedNum(i);
                                    break;
                                }
                            }
                            if(leftClickType)leftClickType=false;
                        }
                    }
                }
            }
        });
    }


    private void initAdapter(){
        leftAdapter = new GoodsClassifyRVAdapter(getActivity(),dishMenuList);
        rightAdapter = new GoodsListRVAdapter(getActivity(),dishMenuList,shopCart);
        rightMenu.setAdapter(rightAdapter);
        leftMenu.setAdapter(leftAdapter);
        /**
         * 左侧适配器添加单机选中监听
         */
        leftAdapter.addItemSelectedListener(this);
        /**
         * 右侧适配器设置购物车
         */
        rightAdapter.setShopCartImp(this);
        initHeadView();
    }

    private void initHeadView(){
        headMenu = rightAdapter.getMenuOfMenuByPosition(0);
        headerLayout.setContentDescription("0");
        headerView.setText(headMenu.getMenuName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        leftAdapter.removeItemSelectedListener(this);
    }

    private void showHeadView(){
        headerLayout.setTranslationY(0);
        View underView = rightMenu.findChildViewUnder(headerView.getX(),0);
        if(underView!=null && underView.getContentDescription()!=null){
            int position = Integer.parseInt(underView.getContentDescription().toString());
            DishMenu menu = rightAdapter.getMenuOfMenuByPosition(position+1);
            headMenu = menu;
            if(headMenu!=null){
                if(headMenu.getMenuName()!=null){
                    headerView.setText(headMenu.getMenuName());
                }
            }

            for (int i = 0; i < dishMenuList.size(); i++) {
                if (dishMenuList.get(i) == headMenu) {
                    leftAdapter.setSelectedNum(i);
                    break;
                }
            }
        }
    }

    @Override
    public void onLeftItemSelected(int position, DishMenu menu) {
        int sum=0;
        for(int i = 0;i<position;i++){
            sum+=dishMenuList.get(i).getDishList().size()+1;
        }
        LinearLayoutManager layoutManager = (LinearLayoutManager) rightMenu.getLayoutManager();
        layoutManager.scrollToPositionWithOffset(sum,0);
        leftClickType = true;
    }

    @Override
    public void add(View view,int position) {
       /* int[] addLocation = new int[2];
        int[] cartLocation = new int[2];
        int[] recycleLocation = new int[2];

        view.getLocationInWindow(addLocation);
        shoppingCartView.getLocationInWindow(cartLocation);
        rightMenu.getLocationInWindow(recycleLocation);

        PointF startP = new PointF();
        PointF endP = new PointF();
        PointF controlP = new PointF();

        startP.x = addLocation[0];
        startP.y = addLocation[1]-recycleLocation[1]+125;
        Log.e("xxx:",startP.y+"");
        endP.x = cartLocation[0];
        endP.y = cartLocation[1]-recycleLocation[1];
        controlP.x = endP.x;
        controlP.y = startP.y;

        final FakeAddImageView fakeAddImageView = new FakeAddImageView(getActivity());
        mainLayout.addView(fakeAddImageView);
        fakeAddImageView.setImageResource(R.mipmap.icon_add);
        fakeAddImageView.getLayoutParams().width = getResources().getDimensionPixelSize(R.dimen.item_dish_circle_size);
        fakeAddImageView.getLayoutParams().height = getResources().getDimensionPixelSize(R.dimen.item_dish_circle_size);
        fakeAddImageView.setVisibility(View.VISIBLE);
        ObjectAnimator addAnimator = ObjectAnimator.ofObject(fakeAddImageView, "mPointF",
                new PointFTypeEvaluator(controlP), startP, endP);
        addAnimator.setInterpolator(new AccelerateInterpolator());
        addAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                fakeAddImageView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                fakeAddImageView.setVisibility(View.GONE);
                mainLayout.removeView(fakeAddImageView);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        ObjectAnimator scaleAnimatorX = new ObjectAnimator().ofFloat(shoppingCartView,"scaleX", 0.6f, 1.0f);
        ObjectAnimator scaleAnimatorY = new ObjectAnimator().ofFloat(shoppingCartView,"scaleY", 0.6f, 1.0f);
        scaleAnimatorX.setInterpolator(new AccelerateInterpolator());
        scaleAnimatorY.setInterpolator(new AccelerateInterpolator());
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleAnimatorX).with(scaleAnimatorY).after(addAnimator);
        animatorSet.setDuration(700);
        animatorSet.start();
*/
        showTotalPrice();
    }

    @Override
    public void remove(View view,int position) {
        showTotalPrice();
    }

    /**
     * 显示价格
     */
    private void showTotalPrice(){
        if(shopCart!=null && shopCart.getShoppingTotalPrice()>0){
            totalPriceTextView.setVisibility(View.VISIBLE);
            totalPriceTextView.setText("￥ "+ NumformatUtil.save2(shopCart.getShoppingTotalPrice()));
            totalPriceNumTextView.setVisibility(View.VISIBLE);
            totalPriceNumTextView.setText(""+shopCart.getShoppingAccount());

        }else {
            totalPriceTextView.setVisibility(View.GONE);
            totalPriceNumTextView.setVisibility(View.GONE);
        }

    }

    /**
     * 返回购物车对象
     * @return
     */
    public ShopCart getShopCart(){
        return shopCart;
    }

}