import React from 'react';

const PlantCard = ({ plant }) => {
  const handleEdit = () => {
    alert(`Edit Plant: ${plant.nickname}`);
  };

  const handleDelete = () => {
    if (window.confirm(`Are you sure you want to delete the plant: ${plant.nickname}?`)) {
      alert(`Deleted plant: ${plant.nickname}`);
      // Add delete logic here (call delete API)
    }
  };

  return (
    <div className="card">
      <img src={plant.imageUrl || '/default-plant.jpg'} alt={plant.nickname} />
      <h3>{plant.nickname}</h3>
      <p><strong>Status:</strong> {plant.healthStatus}</p>
      <p><strong>Location:</strong> {plant.location}</p>
      <p><strong>Notes:</strong> {plant.notes}</p>
      
      <div className="card-actions">
        <button onClick={handleEdit}>Edit</button>
        <button onClick={handleDelete}>Delete</button>
      </div>
    </div>
  );
};

export default PlantCard;
