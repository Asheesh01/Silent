import React, { useEffect, useState } from "react";
import Card from "../card/card";
import ThumbUpAltIcon from '@mui/icons-material/ThumbUpAlt';
import ThumbUpOutlinedIcon from '@mui/icons-material/ThumbUpOutlined';
import CommentOutlinedIcon from '@mui/icons-material/CommentOutlined';
import SendOutlinedIcon from '@mui/icons-material/SendOutlined';
import { Link } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
export default function Post({ profile, item, key, personalData }) {


    const [seeMore, setSeeMore] = useState(false);
    const [comment, setComment] = useState(false);
    const [commentInput, setCommentInput] = useState("");
    const [comments, setComments] = useState([]);

    const[like,setlike]=useState(false);
   const [noOfLikes, setNoOfLikes] = useState(Array.isArray(item?.likes) ? item.likes.length : 0);



    const handleSubmitComment = async (e) => {
    e.preventDefault();

    if(commentInput.trim().length===0){
        return toast.error("Please Enter Something")
    }
    
    try {
        const response = await axios.post("http://localhost:5000/api/comment", {
            postId: item._id,
            comment: commentInput
        }, { withCredentials: true });
        
        setComments([response.data.comment, ...comments]);
        setCommentInput("");
    } catch (error) {
        console.error("Error posting comment:", error);
        alert("Failed to post comment");
    }
};


    useEffect(()=>{
        if (!personalData || !personalData._id) {
        return;
    }
        let selfId=personalData?._id;
        item?.likes?.map((item)=>{
            if(item.toString()===selfId.toString()){
                setlike(true);
                return;
            } else{
                setlike(false);
            }
        })
    },[])


   const handleLikeFunc=async()=>{
    await axios.post("http://localhost:5000/api/post/likeDislike",{ postId:item?._id},{withCredentials:true}).then(res=>{
        if(like){
            setNoOfLikes((prev)=>prev-1);
            setlike(false);
        }
        else{
            setlike(true);
            setNoOfLikes((prev)=>prev+1);
        }
    }).catch(err=>{
        console.log(err)
        alert('Something went Wrong ')
    })
   }


   const handleCommentBoxOpenCLose=async()=>{
    setComment(true);
    await axios.get(`http://localhost:5000/api/comment/${item._id}`).then(res=>{
        console.log(res);
        setComments(res.data.comments)

    }).catch(err=>{
        console.log(err)
        alert('Something went Wrong ')
   })
}

const copyToClipBoard=async()=>{
    try{
            let string=`http://localhost:5000/profile/${item?.user?._id}/activities/${item?._id}`
            await navigator.clipboard.writeText(string);
            toast.success('copied to Clipboard')
    } catch(err){
        console.err('Failed to Copy!',err);
    }
}

    const desc = item?.desc;
    return (
       
       
        // inside Post.jsx return

<Card padding={0}>
  <div className="flex gap-3 p-4">
    <Link to={`/profile/${item?.user?._id}`} className="w-[48px] h-[48px] rounded-4xl">
      <img
        className="w-[48px] h-[48px] rounded-full border-white cursor-pointer"
        src={item?.user?.profile_pic}
        alt="Post"
      />
    </Link>

    <div>
      <div className="text-lg font-semibold">{item?.user?.f_name}</div>
      <div className="text-xs text-gray-500">{item?.user?.headline}</div>
    </div>
  </div>

  {/* 🔗 Wrap post content with Link to single post */}
  <Link to={`/profile/${item?.user?._id}/activities/${item?._id}`} className="block">
    {desc?.length > 0 && (
      <div className="text-md p-4 my-3 whitespace-pre-line flex-grow">
        {seeMore
          ? desc
          : desc?.length > 50
          ? `${desc.slice(0, 50)}...`
          : `${desc}`}
        {desc?.length > 50 && (
          <span
            onClick={(e) => {
              e.preventDefault(); // prevent navigation when toggling text
              setSeeMore((p) => !p);
            }}
            className="cursor-pointer text-gray-500"
          >
            {seeMore ? "See Less" : "See More"}
          </span>
        )}
      </div>
    )}

    {item?.image && (
      <div className="w-[100%] h-[300px]">
        <img className="w-full h-full object-cover" src={item.image} alt="Post" />
      </div>
    )}
  </Link>



            <div className="my-2 p-4 flex justify-between items-center">
                <div className="flex gap-1 items-center">
                    <ThumbUpAltIcon sx={{ color: "blue", fontSize: 16 }} /> <div className="cursor-pointer text-sm text-gray-600">{noOfLikes}</div>
                </div>

                <div className="flex gap-1 items-center">
                    <div className="cursor-pointer text-sm text-gray-600">{comments.length}comments</div>
                </div>
            </div>
            {
                !profile && <div className="flex p-1">
                    <div onClick={handleLikeFunc} className="w-[33%] justify-center flex gap-2 items-center border-r-1 border-gray-100 p-2 cursor-pointer hover:bg-gray-100" >{like?<ThumbUpAltIcon sx={{ fontSize: 22, color: "blue" }} />:<ThumbUpOutlinedIcon sx={{fontSize:22,color:"blue"}}/>} <span>{like?'Liked':'Like'}</span> </div>
                    <div onClick={handleCommentBoxOpenCLose} className="w-[33%] justify-center flex gap-2 items-center border-r-1 border-gray-100 p-2 cursor-pointer  hover:bg-gray-100 "><CommentOutlinedIcon sx={{ fontSize: 22 }} /> <span>Comment</span> </div>
                    <div onClick={copyToClipBoard} className="w-[33%] justify-center flex gap-2 items-center border-r-1 border-gray-100 p-2 cursor-pointer hover:bg-gray-100 "><SendOutlinedIcon sx={{ fontSize: 22 }} /> <span>Share</span> </div>

                </div>
            }
                    

            {/* Comment section */}
            {comment && (
            <div className="p-4 w-full">
                <div className="flex gap-2 items-center" >
                    <img className="rounded-full w-[48px] h-[48px] border-2 border-white cursor-pointer" src={personalData?.profile_pic} alt=""/>
                    <form className="w-full flex gap-2" onSubmit={handleSubmitComment}>
                        <input
                            type="text"
                            placeholder="Add a Comment"
                            className="w-full border-[2px] py-3 px-5 rounded-3xl hover:bg-gray-100"
                            value={commentInput}                         // ✅ bind state
                            onChange={(e) => setCommentInput(e.target.value)}  // ✅ update state
                        />

                        <button type="submit" className="bg-purple-900 w-[60px] h-[45px] rounded-2xl hover:bg-purple-800 cursor-pointer flex items-center text-white py-1  px-3">Send</button>
                    </form>

                </div>


                {/* Other's comment section */}
                {/* Display comments */}
       
                    <div className="w-full p-4">
                        {comments.map((item, index) => (
                            <div key={index} className="my-4">
                                <Link to={`/profile/${item?.user?._id}`} className="flex gap-3">
                                    <img 
                                        className="rounded-full w-[40px] h-[35px] border-2 border-white cursor-pointer"
                                        src={item?.user?.profile_pic}
                                        alt=""
                                    />
                                    <div className="cursor-pointer">
                                        <div className="text-md">{item?.user?.f_name || "You"}</div>
                                        <div className="text-sm text-gray-500">@{item?.headline || "Member"}</div>
                                    </div>
                                </Link>
                                <div className="px-[44px] my-2">{item?.comment}</div>
                            </div>
                        ))}
                    </div>
                


            </div>
            )}
        </Card>
    )
   
}


















 