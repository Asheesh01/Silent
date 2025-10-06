    import React, { useState } from "react";
    import ImageIcon from '@mui/icons-material/Image';
    import { ToastContainer, toast } from "react-toastify";
    import axios from "axios";
    export default function AddModel(props) {

      const [imageUrl, setImageUrl] = useState(null);
      const [desc, setdesc] = useState("")
      const handlePost = async () => {
        if (desc.trim().length === 0 && !imageUrl) return toast.error("Please enter any field");
        await axios.post("http://localhost:5000/api/post",{desc:desc,image:imageUrl},{withCredentials:true}).then((res)=>{
          window.location.reload();
        }).catch (err=> {
          console.error(err);
          toast.error("Upload failed");
      })
    }

      const handleInputImage = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        const data = new FormData();
        data.append("file", file);
        data.append("upload_preset", "my_unsigned_preset"); // from Cloudinary

        try {
          const res = await fetch(
            "https://api.cloudinary.com/v1_1/dmlz1qzzr/image/upload",
            {
              method: "POST",
              body: data,
            }
          );
          const result = await res.json();
          setImageUrl(result.secure_url); // Cloudinary URL
          toast.success("Image uploaded!");
        } catch (err) {
          console.error(err);
          toast.error("Upload failed");
        }
      };

      return (
        <div>
          <div className="flex gap-4 items-center">
            <div className="relative">
              <img className="w-[60px] h-[60px] rounded-full" src={props.personalData?.profile_pic} alt="Img" />

            </div>
            <div className="text-2xl">{props.personalData?.f_name}</div>
          </div>

          <div>
            <textarea value={desc} onChange={(e) => setdesc(e.target.value)} cols={50} rows={5} placeholder="What do you want to talk about" className="my-3 outline-0 text-xl p-2"></textarea>
          </div>
          {
            imageUrl && <div>
              <img className="w-[80px] h-[80px] mt-1 rounded-2xl" src={imageUrl} alt="" />
            </div>
          }
          <div className="flex justify-between items-center">
            <div className="my-6">
              <label className="cursor-pointer" htmlFor="inputfile"><ImageIcon /></label>
              <input type="file" className="hidden" id="inputfile" accept="image/*" onChange={handleInputImage} />
            </div>
            <div className="bg-blue-950 text-white py-1 px-3 h-fit cursor-pointer rounded-2xl" onClick={handlePost} >Post</div>
          </div>


        </div>
      )
    }